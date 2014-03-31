package de.fu_berlin.inf.dpp.core.project;


import de.fu_berlin.inf.dpp.core.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.OutgoingSessionNegotiation;

/**
 * Created by IntelliJ IDEA.
 * User: r.kvietkauskas
 * Date: 14.3.14
 * Time: 13.12
 * To change this template use File | Settings | File Templates.
 */
public interface INegotiationHandler
{
    /**
     * Called when a session invitation is offered to a contact.
     *
     * @param negotiation the negotiation process to handle the invitation
     */
    public void handleOutgoingSessionNegotiation(
            OutgoingSessionNegotiation negotiation);

    /**
     * Called when an invitation to a session is received from a contact.
     *
     * @param negotiation the negotiation process to handle the invitation
     */
    public void handleIncomingSessionNegotiation(
            IncomingSessionNegotiation negotiation);

    /**
     * Called when a local project should be synchronized with a remote session
     * user.
     *
     * @param negotiation the negotiation process to handle the project synchronization
     */
    public void handleOutgoingProjectNegotiation(
            OutgoingProjectNegotiation negotiation);

    /**
     * Called when a remote project from a remote session user should be
     * synchronized with a local project.
     *
     * @param negotiation the negotiation process to handle the project synchronization
     */
    public void handleIncomingProjectNegotiation(
            IncomingProjectNegotiation negotiation);
}
