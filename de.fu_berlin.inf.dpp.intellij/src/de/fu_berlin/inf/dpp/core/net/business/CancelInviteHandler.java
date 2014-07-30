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
import de.fu_berlin.inf.dpp.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.SessionNegotiationObservable;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * Listens for {@link CancelInviteExtension}-packets that cancel the invitation
 * process and cancels it locally.
 */
public class CancelInviteHandler {

    private static final Logger LOG = Logger
        .getLogger(CancelInviteHandler.class.getName());

    private final SessionNegotiationObservable invitationProcesses;

    private final PacketListener cancelInvitationExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelInviteExtension extension = CancelInviteExtension.PROVIDER
                .getPayload(packet);

            invitationCanceled(new JID(packet.getFrom()),
                extension.getNegotiationID(), extension.getErrorMessage());
        }
    };

    public CancelInviteHandler(IReceiver receiver,
        SessionNegotiationObservable invitationProcessObservable) {

        invitationProcesses = invitationProcessObservable;

        receiver.addPacketListener(cancelInvitationExtensionListener,
            CancelInviteExtension.PROVIDER.getPacketFilter());
    }

    private void invitationCanceled(JID sender, String invitationID,
        String errorMsg) {

        SessionNegotiation invitationProcess = invitationProcesses
            .get(sender, invitationID);

        if (invitationProcess == null) {
            LOG.warn(
                "Inv[unkown user]: Received invitation cancel message for unknown invitation process. Ignoring...");
            return;
        }

        LOG.debug("Inv" + sender + " : Received invitation cancel message");

        invitationProcess.remoteCancel(errorMsg);
    }
}