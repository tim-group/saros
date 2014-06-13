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
 * /
 */

package de.fu_berlin.inf.dpp.core.net.business;

import de.fu_berlin.inf.dpp.communication.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;


import de.fu_berlin.inf.dpp.net.IReceiver;


import de.fu_berlin.inf.dpp.session.ISarosSession;

public class CancelProjectSharingHandler {

    private static final Logger log = Logger
            .getLogger(CancelProjectSharingHandler.class.getName());

    private final ISarosSessionManager sessionManager;

    private final ProjectNegotiationObservable projectExchangeProcesses;

    private final IReceiver receiver;

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(cancelProjectNegotiationListener,
                    CancelProjectNegotiationExtension.PROVIDER
                            .getPacketFilter(session.getID()));
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(cancelProjectNegotiationListener);
        }
    };

    private final PacketListener cancelProjectNegotiationListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelProjectNegotiationExtension extension = CancelProjectNegotiationExtension.PROVIDER
                    .getPayload(packet);
            projectSharingCanceled(new JID(packet.getFrom()),
                    extension.getErrorMessage());
        }
    };

    public CancelProjectSharingHandler(IReceiver receiver,
            ISarosSessionManager sessionManager,
            ProjectNegotiationObservable projectNegotiationObservable) {

        this.receiver = receiver;

        this.sessionManager = sessionManager;
        this.projectExchangeProcesses = projectNegotiationObservable;

        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    public void projectSharingCanceled(JID sender, String errorMsg) {

        ProjectNegotiation process = projectExchangeProcesses
                .getProjectExchangeProcess(sender);
        if (process != null) {
            log.debug("Inv" + sender + " : Received invitation cancel message");
            process.remoteCancel(errorMsg);
        } else {
            log.warn("Inv[unkown user]: Received invitation cancel message");
        }
    }
}
