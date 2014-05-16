package de.fu_berlin.inf.dpp.core.invitation;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import de.fu_berlin.inf.dpp.core.context.AbstractSaros;
import de.fu_berlin.inf.dpp.core.context.ISarosContext;
import de.fu_berlin.inf.dpp.core.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.core.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.core.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.core.invitation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.project.internal.ColorNegotiationHook;
import de.fu_berlin.inf.dpp.core.versioning.Compatibility;
import de.fu_berlin.inf.dpp.core.versioning.VersionCompatibilityResult;
import de.fu_berlin.inf.dpp.core.versioning.VersionManager;
import de.fu_berlin.inf.dpp.core.invitation.ProcessTools.CancelOption;
import org.apache.log4j.Logger;

import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;


import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParameterExchangeExtension;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;


/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public final class OutgoingSessionNegotiation extends SessionNegotiation {

    private static final Logger log = Logger
            .getLogger(OutgoingSessionNegotiation.class);

    private static final boolean IGNORE_VERSION_COMPATIBILITY = Boolean
            .getBoolean("de.fu_berlin.inf.dpp.negotiation.session.IGNORE_VERSION_COMPATIBILITY");

    private static final Random INVITATION_ID_GENERATOR = new Random();

    private ISarosSession sarosSession;

    private String localVersion;

    private SarosPacketCollector invitationAcceptedCollector;
    private SarosPacketCollector invitationAcknowledgedCollector;
    private SarosPacketCollector invitationDataExchangeCollector;
    private SarosPacketCollector invitationCompletedCollector;

    @Inject
    private VersionManager versionManager;

    @Inject
    private DiscoveryManager discoveryManager;

    @Inject
    private DataTransferManager dataTransferManager;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private InvitationProcessObservable currentSessionNegotiations;

    // HACK last residue of the direct conncetion between SessionNegotation and
    // the color property of users.
    private int clientColorID = UserColorID.UNKNOWN;
    private int clientFavoriteColorID = UserColorID.UNKNOWN;

    public OutgoingSessionNegotiation(JID peer, ISarosSession sarosSession,
            String description, ISarosContext sarosContext) {

        super(String.valueOf(INVITATION_ID_GENERATOR.nextLong()), peer,
                description, sarosContext);

        this.sarosSession = sarosSession;
    }

    @Override
    protected void executeCancellation() {
        // TODO remove the user from the session !

        if (currentSessionNegotiations.getProcesses().size() == 0
                && sarosSession.getRemoteUsers().isEmpty())
            sarosSessionManager.stopSarosSession();
    }


    /**
     * @JTourBusStop 5, Invitation Process:
     *
     *               The details of the invitation process are implemented in
     *               the invitation package. OutgoingSessionNegotiation is an
     *               example of a class that participates in this process.
     *
     *               The host of a session needs negotiations for:
     *
     *               - Sending invitation to a session
     *               (OutgoingSessionNegotiation)
     *
     *               - Sending project resources included in a session
     *               (OutgoingProjectNegotiation)
     *
     *               All other participants need negotiations for:
     *
     *               - Dealing with a received invitation to a session
     *               (IncomingSessionNegotiation)
     *
     *               - Handling incoming shared project resources
     *               (IncomingProjectNegotiation)
     */
    public Status start(IProgressMonitor monitor) {
        log.debug(this + " : starting invitation");

        observeMonitor(monitor);

        monitor.beginTask("Inviting " + peerNickname + "...",
                IProgressMonitor.UNKNOWN);

        createCollectors();

        Exception exception = null;

        try {
            /**
             * @JTourBusStop 6, Invitation Process:
             *
             *               For starting a session, the host does the following
             *               things (see next JTourBusStops for the
             *               corresponding steps on the client side):
             *
             *               (1) Check whether Saros is available on the
             *               client's side (via the DiscoveryManager).
             *
             *               (2) Check whether the client's Saros is compatible
             *               with own version (via the VersionManager).
             *
             *               (3a) Send a session invitation offering to the
             *               client.
             *
             *               (3b) [client side, see subsequent stops]
             *
             *               (3c) Waits until the client automatically responds
             *               to the offering ("acknowledgement").
             *
             *               (4a, 4b) [client side, see subsequent stops]
             *
             *               (4c) Wait until the remote user manually accepted
             *               the session invitation ("acceptance").
             *
             *               (5a) [client side, see subsequent stops]
             *
             *               (5b) Wait for the client's wishlist of the
             *               session's parameters (e.g. his own favorite
             *               color).
             *
             *               (6a) Consider these preferences and send the
             *               settled session parameters back to the client.
             *
             *               (6b, 7, 8) [client side, see subsequent stops]
             *
             *               (9) Wait until the client signals the session
             *               invitation is complete.
             *
             *               (10) Formally add client to the session so he will
             *               receive activities, then send final acknowledgement
             *               to inform client about this.
             */


            checkAvailability(monitor);

            checkVersion(monitor);

            sendInvitationOffer(monitor);

            awaitAcknowledgement(monitor);

            awaitAcceptance(monitor);

            InvitationParameterExchangeExtension clientSessionPreferences;
            clientSessionPreferences = awaitClientSessionPreferences(monitor);

            InvitationParameterExchangeExtension actualSessionParameters;
            actualSessionParameters = determineSessionParameters(clientSessionPreferences);

            sendSessionParameters(actualSessionParameters, monitor);

            awaitCompletion(monitor);

            monitor.setTaskName("Negotiating data connection...");

            dataTransferManager.connect(ISarosSession.SESSION_CONNECTION_ID,
                    peer);

            User newUser = completeInvitation(monitor);

            monitor.done();

            // Whiteboard is using this listener
            sessionManager.postOutgoingInvitationCompleted(monitor, newUser);

        } catch (Exception e) {

            e.printStackTrace();

            exception = e;
        } finally {
            deleteCollectors();
            monitor.done();
        }

        return terminateProcess(exception);
    }

    /**
     * Performs a discovery request on the remote side and checks for Saros
     * support. When this method returns, the remote JID (see
     * {@link SessionNegotiation#peer}) has been properly updated to a full
     * resource qualified JID.
     */
    private void checkAvailability(IProgressMonitor monitor)
            throws LocalCancellationException
    {

        log.debug(this + " : checking Saros support");
        monitor.setTaskName("Checking Saros support...");

        JID resourceQualifiedJID = discoveryManager.getSupportingPresence(peer,
                AbstractSaros.NAMESPACE);

        if (resourceQualifiedJID == null)
            throw new LocalCancellationException(
                    peerNickname
                            + " does not support Saros or the request timed out. Please try again.",
                    ProcessTools.CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " :  remote contact offers Saros support");

        peer = resourceQualifiedJID;
    }

    /**
     * Checks the compatibility of the local Saros version with the remote side.
     * If the versions are compatible, the invitation continues. Otherwise, the
     * invitation is cancelled locally.
     * <p>
     * However, if
     * {@link OutgoingSessionNegotiation#IGNORE_VERSION_COMPATIBILITY} is set to
     * <code>true</code> the invitation process will continue.
     */
    private void checkVersion(IProgressMonitor monitor)
            throws SarosCancellationException
    {

        log.debug(this + " : checking version compatibility");
        monitor.setTaskName("Checking version compatibility...");

        VersionCompatibilityResult result = versionManager
                .determineVersionCompatibility(peer);

        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        if (result == null) {
            log.error(this + " : could not obtain remote Saros version");
            throw new LocalCancellationException(
                    "Could not obtain the version of the Saros plugin from "
                            + peerNickname + ". Please try again.",
                    CancelOption.DO_NOT_NOTIFY_PEER);
        }

        Compatibility comp = result.getCompatibility();

        if (comp != Compatibility.OK && !IGNORE_VERSION_COMPATIBILITY) {
            log.error(this + " : Saros versions are not compatible");
            throw new LocalCancellationException(
                    "The Saros plugin of "
                            + peerNickname
                            + " (Version "
                            + result.getRemoteVersion()
                            + ") is not compatible with your installed Saros plugin (Version "
                            + result.getLocalVersion() + ")",
                    CancelOption.DO_NOT_NOTIFY_PEER);
        }

        if (comp == Compatibility.OK)
            log.debug(this + " : Saros versions are compatible");
        else
            log.warn(this + " : Saros versions are not compatible");

        localVersion = result.getLocalVersion().toString();
    }

    /**
     * Sends an invitation offer to the client.
     */
    private void sendInvitationOffer(IProgressMonitor monitor)
            throws SarosCancellationException {
        monitor.setTaskName("Sending invitation...");

        log.debug(this + " : sending invitation");
        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        InvitationOfferingExtension invitationOffering = new InvitationOfferingExtension(
                invitationID, sarosSession.getID(), localVersion, description);

        transmitter.sendMessageToUser(peer,
                InvitationOfferingExtension.PROVIDER.create(invitationOffering));
    }

    /**
     * Waits for the client's acknowledgment for the invitation offering. The
     * acknowledgment is auto-generated on the remote side.
     */
    private void awaitAcknowledgement(IProgressMonitor monitor)
            throws SarosCancellationException {
        log.debug(this + " : waiting for invitation acknowledgement");

        monitor.setTaskName("Waiting for " + peerNickname
                + " to acknowledge the invitation...");

        if (collectPacket(invitationAcknowledgedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                    "Received no invitation acknowledgement from " + peerNickname
                            + ".", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        log.debug(this + " : invitation acknowledged");
    }

    /**
     * Waits until the remote side manually accepts the invitation.
     */
    private void awaitAcceptance(IProgressMonitor monitor)
            throws SarosCancellationException {

        log.debug(this + " : waiting for peer to next the invitation");

        monitor.setTaskName("Waiting for " + peerNickname
                + " to next invitation...");

        if (collectPacket(invitationAcceptedCollector,
                INVITATION_ACCEPTED_TIMEOUT) == null) {
            throw new LocalCancellationException(
                    "Invitation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : invitation accepted");
    }

    /**
     * Waits for the client's session parameters. They may contain some desired
     * default values that should be used on session first.
     */
    private InvitationParameterExchangeExtension awaitClientSessionPreferences(
            IProgressMonitor monitor) throws SarosCancellationException {

        log.debug(this + " : waiting for client's session parameters");

        monitor.setTaskName("Waiting for client's session parameters...");

        Packet packet = collectPacket(invitationDataExchangeCollector,
                PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException(peerNickname
                    + " does not respond. (Timeout)",
                    CancelOption.DO_NOT_NOTIFY_PEER);

        InvitationParameterExchangeExtension parameters;
        parameters = InvitationParameterExchangeExtension.PROVIDER
                .getPayload(packet);

        if (parameters == null)
            throw new LocalCancellationException(peerNickname
                    + " sent malformed data", CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " : received client's session parameters");

        return parameters;
    }

    /**
     * Checks and modifies the received remote parameters.
     */
    private InvitationParameterExchangeExtension determineSessionParameters(
            InvitationParameterExchangeExtension clientParameters) {

        // general purpose
        InvitationParameterExchangeExtension hostParameters = new InvitationParameterExchangeExtension(
                invitationID);

        hostParameters.setSessionHost(sarosSession.getHost().getJID());

        // call each hook to do its magic
        for (ISessionNegotiationHook hook : hookManager.getHooks()) {
            Map<String, String> preferredSettings = clientParameters
                    .getHookSettings(hook);
            Map<String, String> actualSettings = hook
                    .considerClientPreferences(preferredSettings);

            hostParameters.saveHookSettings(hook, actualSettings);

            // HACK A User object representing the client needs to access these
            // to values in completeInvitation(). Color management should work
            // differently.
            if (hook instanceof ColorNegotiationHook) {
                clientColorID = Integer.parseInt(actualSettings
                        .get(ColorNegotiationHook.KEY_CLIENT_COLOR));
                clientFavoriteColorID = Integer.parseInt(actualSettings
                        .get(ColorNegotiationHook.KEY_CLIENT_FAV_COLOR));
            }
        }

        return hostParameters;
    }

    /**
     * The changes will be send back and must then be used on the remote side to
     * configure the session environment.
     */
    private void sendSessionParameters(
            InvitationParameterExchangeExtension modifiedParameters,
            IProgressMonitor monitor) {

        log.debug(this + " : sending updated session negotiation data");

        monitor.setTaskName("Sending local session configuration...");
        transmitter.sendMessageToUser(peer,
                InvitationParameterExchangeExtension.PROVIDER
                        .create(modifiedParameters));

        log.debug(this + " : sent updated session negotiation data");
    }

    /**
     * Waits until the remote side has completed the invitation. This is the
     * case after the remote side has started its {@link SarosSession}.
     */
    private void awaitCompletion(IProgressMonitor monitor)
            throws SarosCancellationException {

        log.debug(this
                + " : waiting for remote side to first its Saros session");

        monitor.setTaskName("Waiting for " + peerNickname
                + " to perform final initialization...");

        if (collectPacket(invitationCompletedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                    "Invitation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : remote side started its Saros session");
    }

    /**
     *
     * Adds the invited user to the current SarosSession. After the user is
     * added to the session the user list is synchronized and afterwards an
     * acknowledgment is send to the remote side that the remote user can now
     * first working in this session.
     *
     * @throws IOException
     */
    private User completeInvitation(IProgressMonitor monitor)
            throws IOException {

        log.debug(this + " : synchronizing user list");

        monitor.setTaskName("Synchronizing user list...");

        User user = new User(peer, false, false, clientColorID,
                clientFavoriteColorID);

        synchronized (CancelableProcess.SHARED_LOCK) {

            sarosSession.addUser(user);
            log.debug(this + " : added " + peer
                    + " to the current session, colorID: " + clientColorID);

            /* *
             *
             * @JTourBusStop 7, Creating custom network messages, Sending custom
             * messages:
             *
             * This is pretty straight forward. Create an instance of your
             * extension with the proper arguments and use the provider to
             * create a (marshalled) packet extension. The extension can now be
             * send using the various methods of the ITransmitted interface.
             */

            transmitter.sendToSessionUser(ISarosSession.SESSION_CONNECTION_ID,
                    peer, InvitationAcknowledgedExtension.PROVIDER
                            .create(new InvitationAcknowledgedExtension(invitationID)));
        }

        log.debug(this + " : session negotiation finished");

        return user;
    }

    private void createCollectors() {

        /* *
         *
         * @JTourBusStop 9, Creating custom network messages, Receiving custom
         * messages - Part 2:
         *
         * Another way to receive custom message is to use a collector which you
         * can poll instead. The same rules as in step 7 applies to the
         * collector as well. Pay attention to the filter you use and avoid
         * using the collector when the current thread context is the context
         * for dispatching messages.
         *
         * IMPORTANT: Your logic must ensure that the collector is cancelled
         * after it is no longer used. Failing to do so will result in memory
         * leaks.
         */

        invitationAcceptedCollector = receiver
                .createCollector(InvitationAcceptedExtension.PROVIDER
                        .getPacketFilter(invitationID));

        invitationAcknowledgedCollector = receiver
                .createCollector(InvitationAcknowledgedExtension.PROVIDER
                        .getPacketFilter(invitationID));

        invitationDataExchangeCollector = receiver
                .createCollector(InvitationParameterExchangeExtension.PROVIDER
                        .getPacketFilter(invitationID));

        invitationCompletedCollector = receiver
                .createCollector(InvitationCompletedExtension.PROVIDER
                        .getPacketFilter(invitationID));
    }

    private void deleteCollectors() {
        invitationAcceptedCollector.cancel();
        invitationAcknowledgedCollector.cancel();
        invitationDataExchangeCollector.cancel();
        invitationCompletedCollector.cancel();
    }

    @Override
    public String toString() {
        return "OSN [remote side: " + peer + "]";
    }
}
