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
import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRequestExtension;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Listens for {@link JoinSessionRequestExtension}s and
 * <ul>
 * <li/> rejects new sessions if we already are in a session
 * <li/> starts a new session, if a new session is requested
 * <li/> adds users to an existing session, if the session already exists
 * </ul>
 */
public final class JoinSessionRequestHandler {

    private static final Logger LOG = Logger
            .getLogger(JoinSessionRequestHandler.class);

    private final PacketListener joinSessionRequestListener = new PacketListener() {

        @Override
        public void processPacket(final Packet packet) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {

                @Override
                public void run() {
                    handleInvitationRequest(new JID(packet.getFrom()),
                            JoinSessionRequestExtension.PROVIDER.getPayload(packet));
                }
            });
        }
    };

    private final ISarosSessionManager sessionManager;
    private final ITransmitter transmitter;
    private final IReceiver receiver;
    private final IPreferenceStore preferenceStore;

    public JoinSessionRequestHandler(ISarosSessionManager sessionManager,
                                     ITransmitter transmitter, IReceiver receiver,
                                     IPreferenceStore preferenceStore) {
        this.sessionManager = sessionManager;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.preferenceStore = preferenceStore;

        if (Boolean.getBoolean("de.fu_berlin.inf.dpp.server.SUPPORTED")) {
            this.receiver.addPacketListener(joinSessionRequestListener,
                    JoinSessionRequestExtension.PROVIDER.getPacketFilter());
        }
    }

    private void handleInvitationRequest(JID from,
                                         JoinSessionRequestExtension extension) {

        ISarosSession session = sessionManager.getSarosSession();

        if (session != null && !session.isHost())
            return;

        if (!preferenceStore.getBoolean(PreferenceConstants.SERVER_ACTIVATED)
                || (session != null && extension.isNewSessionRequested())
                || (session == null && !extension.isNewSessionRequested())) {
            sendRejection(from);
            return;
        }

        List<JID> list = Collections.singletonList(from);

        // TODO removeAll calls to CollaborationUtils
        if (extension.isNewSessionRequested()) {
            CollaborationUtils.startSession(new ArrayList<IResource>(), list);
        } else {
            CollaborationUtils.addContactsToSession(list);
        }
    }

    private void sendRejection(JID to) {
        transmitter.sendPacketExtension(to, JoinSessionRejectedExtension.PROVIDER
                .create(new JoinSessionRejectedExtension()));
    }
}
