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

package de.fu_berlin.inf.dpp.core.ui.eventhandler;

import com.intellij.openapi.application.ApplicationManager;
import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRejectedExtension;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * Listens for {@link JoinSessionRejectedExtension}s and notifies the User
 * if the request to join the session was cancelled.
 */
public final class JoinSessionRejectedHandler {

    private static final Logger LOG = Logger
            .getLogger(JoinSessionRejectedHandler.class);
    private final PacketListener joinSessionRejectedListener = new PacketListener() {

        @Override
        public void processPacket(final Packet packet) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {

                @Override
                public void run() {
                    handleRejection(new JID(packet.getFrom()),
                            JoinSessionRejectedExtension.PROVIDER
                                    .getPayload(packet)
                    );
                }
            });
        }
    };
    private final IReceiver receiver;

    /**
     * Adds {@link #joinSessionRejectedListener}
     *
     * @param receiver
     */
    public JoinSessionRejectedHandler(IReceiver receiver) {
        this.receiver = receiver;
        this.receiver.addPacketListener(joinSessionRejectedListener,
                JoinSessionRejectedExtension.PROVIDER.getPacketFilter());

    }

    private void handleRejection(JID from,
                                 JoinSessionRejectedExtension extension) {

        String name = XMPPUtils.getNickname(null, from);

        if (name == null) {
            name = from.getBase();
        }

        DialogUtils.showInfo(DialogUtils.getDefaultContainer(),
                "Join Session Request Rejected",
                "Your request to join the session of " + name + " was rejected.");
    }
}
