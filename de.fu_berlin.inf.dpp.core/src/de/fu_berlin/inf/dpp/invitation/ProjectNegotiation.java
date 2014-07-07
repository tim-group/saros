package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.MonitorableFileTransfer;
import de.fu_berlin.inf.dpp.monitoring.MonitorableFileTransfer.TransferStatus;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.vcs.VCSProviderFactory;

/**
 * This abstract class is the superclass for {@link OutgoingProjectNegotiation}
 * and {@link IncomingProjectNegotiation}.
 */
public abstract class ProjectNegotiation extends CancelableProcess {

    private static final Logger LOG = Logger
        .getLogger(ProjectNegotiation.class);

    /** Prefix part of the id used in the SMACK XMPP file transfer protocol. */
    public static final String ARCHIVE_TRANSFER_ID = "saros-dpp-pn-server-client-archive/";

    /**
     * Delimiter for every Zip entry to delimit the project id from the path
     * entry.
     * <p>
     * E.g: <b>12345:foo/bar/foobar.java</b>
     */
    protected static final String PATH_DELIMITER = ":";

    /**
     * Timeout for all packet exchanges during the project negotiation
     */
    protected static final long PACKET_TIMEOUT = Long.getLong(
        "de.fu_berlin.inf.dpp.negotiation.project.PACKET_TIMEOUT", 30000L);

    protected String processID;
    protected JID peer;

    protected final String sessionID;

    @Inject
    protected XMPPConnectionService connectionService;

    @Inject
    protected ITransmitter transmitter;

    @Inject
    protected IReceiver xmppReceiver;

    @Inject
    protected VCSProviderFactory vcsProviderFactory;
    /**
     * The file transfer manager can be <code>null</code> if no connection was
     * established or was lost when the class was instantiated.
     */
    protected FileTransferManager fileTransferManager;

    public ProjectNegotiation(JID peer, String sessionID,
        ISarosContext sarosContext) {
        this.peer = peer;
        this.sessionID = sessionID;
        sarosContext.initComponent(this);

        Connection connection = connectionService.getConnection();

        if (connection != null)
            fileTransferManager = new FileTransferManager(connection);
    }

    /**
     * 
     * @return the names of the projects that are shared by the peer. projectID
     *         => projectName
     */
    public abstract Map<String, String> getProjectNames();

    public String getProcessID() {
        return this.processID;
    }

    public JID getPeer() {
        return this.peer;
    }

    @Override
    protected void notifyCancellation(SarosCancellationException exception) {

        if (!(exception instanceof LocalCancellationException))
            return;

        LocalCancellationException cause = (LocalCancellationException) exception;

        if (cause.getCancelOption() != CancelOption.NOTIFY_PEER)
            return;

        LOG.debug("notifying remote contact " + peer
            + " of the local project negotiation cancellation");

        PacketExtension notification = CancelProjectNegotiationExtension.PROVIDER
            .create(new CancelProjectNegotiationExtension(sessionID, cause
                .getMessage()));

        try {
            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, getPeer(),
                notification);
        } catch (IOException e) {
            transmitter.sendPacketExtension(getPeer(), notification);
        }
    }

    /**
     * Monitors a {@link FileTransfer} and waits until it is completed or
     * aborted.
     * 
     * @param transfer
     *            the transfer to monitor
     * @param monitor
     *            the progress monitor that is <b>already initialized</b> to
     *            consume <b>100 ticks</b> to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts <code>null</code>, indicating that no
     *            progress should be reported and that the operation cannot be
     *            cancelled.
     * 
     * @throws SarosCancellationException
     *             if the transfer was aborted either on local side or remote
     *             side, see also {@link LocalCancellationException} and
     *             {@link RemoteCancellationException}
     * @throws IOException
     *             if an I/O error occurred
     */
    protected void monitorFileTransfer(FileTransfer transfer,
        IProgressMonitor monitor) throws SarosCancellationException,
        IOException {

        MonitorableFileTransfer mtf = new MonitorableFileTransfer(transfer,
            monitor);
        TransferStatus transferStatus = mtf.monitorTransfer();

        // some information can be directly read from the returned status
        if (transferStatus.equals(TransferStatus.OK))
            return;

        if (transferStatus.equals(TransferStatus.ERROR)) {
            FileTransfer.Error error = transfer.getError();
            throw new IOException(
                error == null ? "unknown SMACK Filetransfer API error"
                    : error.getMessage(), transfer.getException());
        }

        // other information needs to be read from the transfer object
        if (transfer.getStatus().equals(FileTransfer.Status.cancelled)
            && monitor.isCanceled())
            throw new LocalCancellationException();

        throw new RemoteCancellationException(null);
    }

    /**
     * Returns the next packet from a collector.
     * 
     * @param collector
     *            the collector to monitor
     * @param timeout
     *            the amount of time to wait for the next packet (in
     *            milliseconds)
     * @return the collected packet or <code>null</code> if no packet was
     *         received
     * @throws SarosCancellationException
     *             if the process was canceled
     */
    protected final Packet collectPacket(PacketCollector collector, long timeout)
        throws SarosCancellationException {

        Packet packet = null;

        while (timeout > 0) {
            checkCancellation(CancelOption.NOTIFY_PEER);

            packet = collector.nextResult(1000);

            if (packet != null)
                break;

            timeout -= 1000;
        }
        return packet;
    }

    @Override
    protected void notifyTerminated(ProcessListener listener) {
        listener.processTerminated(this);
    }
}