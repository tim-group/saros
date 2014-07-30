/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package de.fu_berlin.inf.dpp.core.net.business;

import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import java.util.List;

/**
 * Business Logic for handling incoming Session- and ProjectNegotiation requests
 */
public class InvitationHandler {

    private static final Logger LOG = Logger.getLogger(InvitationHandler.class
        .getName());

    private final PacketListener invitationOfferingListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            JID fromJID = new JID(packet.getFrom());

            InvitationOfferingExtension invitation = InvitationOfferingExtension.PROVIDER
                .getPayload(packet);

            if (invitation == null) {
                LOG.warn("received invitation from " + fromJID
                    + " that contains malformed payload");
                return;
            }

            String sessionID = invitation.getSessionID();
            String invitationID = invitation.getNegotiationID();
            String version = invitation.getVersion();
            String description = invitation.getDescription();

            LOG.info("received invitation from " + fromJID + " [invitation id: "
                    + invitationID + ", " + "session id: " + sessionID + ", "
                    + "version: " + version + "]"
            );

            /**
             * @JTourBusStop 7, Invitation Process:
             *
             *               (3b) If the invited user (from now on referred
             *               to as "client") receives an invitation (and if
             *               he is not already in a running session), Saros
             *               will send an automatic response to the inviter
             *               (host). Afterwards, the control is handed over
             *               to the SessionManager.
             */
            if (sessionManager.getSarosSession() == null) {
                PacketExtension response = InvitationAcknowledgedExtension.PROVIDER
                    .create(new InvitationAcknowledgedExtension(invitationID));
                transmitter.sendPacketExtension(fromJID, response);

                sessionManager
                    .invitationReceived(fromJID, sessionID, invitationID,
                        version, description);
            } else {
                // TODO This text should be replaced with a cancel ID
                PacketExtension response = CancelInviteExtension.PROVIDER
                    .create(new CancelInviteExtension(invitationID,
                        "I am already in a Saros session and so cannot accept your invitation."));
                transmitter.sendPacketExtension(fromJID, response);
            }
        }
    };

    private final PacketListener projectOfferingListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            JID fromJID = new JID(packet.getFrom());

            ProjectNegotiationOfferingExtension projectNegotiation = ProjectNegotiationOfferingExtension.PROVIDER
                .getPayload(packet);

            if (projectNegotiation == null) {
                LOG.warn("received project negotiation from " + fromJID
                    + " that contains malformed payload");
                return;
            }

            String sessionID = projectNegotiation.getSessionID();
            String negotiationID = projectNegotiation.getNegotiationID();
            List<ProjectNegotiationData> projectInfos = projectNegotiation
                .getProjectNegotiationData();

            if (!sessionManager.getSarosSession().getID().equals(sessionID)) {
                LOG.warn("received project negotiation from " + fromJID
                    + " that is not in the same session");
                return;
            }

            LOG.info("received project negotiation from " + fromJID
                + " with session id: " + sessionID + " and negotiation id: "
                + negotiationID);

            sessionManager
                .incomingProjectReceived(fromJID, projectInfos, negotiationID);
        }
    };

    @Inject
    private ITransmitter transmitter;
    @Inject
    private ISarosSessionManager sessionManager;

    public InvitationHandler(IReceiver receiver) {

        receiver.addPacketListener(invitationOfferingListener,
            InvitationOfferingExtension.PROVIDER.getPacketFilter());
        receiver.addPacketListener(projectOfferingListener,
            ProjectNegotiationOfferingExtension.PROVIDER.getPacketFilter());
    }
}