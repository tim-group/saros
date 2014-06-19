package de.fu_berlin.inf.dpp.core.invitation;


import de.fu_berlin.inf.dpp.core.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.OutgoingSessionNegotiation;


/**
 * Interface for handling incoming and outgoing session and project
 * negotiations.
 *
 * @Note Implementations <b>must not</b> block on all methods that are provided
 *       by this interface. Furthermore it is possible that the methods are
 *       called concurrently.
 *
 * @author srossbach
 */
public interface INegotiationHandler {

    /**
     * Called when a session invitation is offered to a contact.
     *
     * @param negotiation
     *            the negotiation process to handle the invitation
     */
    public void handleOutgoingSessionNegotiation(
            OutgoingSessionNegotiation negotiation);

    /**
     * Called when an invitation to a session is received from a contact.
     *
     * @param negotiation
     *            the negotiation process to handle the invitation
     */
    public void handleIncomingSessionNegotiation(
            IncomingSessionNegotiation negotiation);

    /**
     * Called when a local project should be synchronized with a remote session
     * user.
     *
     * @param negotiation
     *            the negotiation process to handle the project synchronization
     */
    public void handleOutgoingProjectNegotiation(
            OutgoingProjectNegotiation negotiation);

    /**
     * Called when a remote project from a remote session user should be
     * synchronized with a local project.
     *
     * @param negotiation
     *            the negotiation process to handle the project synchronization
     */
    public void handleIncomingProjectNegotiation(
            IncomingProjectNegotiation negotiation);
}
