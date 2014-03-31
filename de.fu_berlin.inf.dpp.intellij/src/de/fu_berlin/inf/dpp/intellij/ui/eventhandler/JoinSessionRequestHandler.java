package de.fu_berlin.inf.dpp.intellij.ui.eventhandler;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinSessionRejectedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinSessionRequestExtension;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import java.util.Collections;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.27
 * Time: 09.33
 */

public final class JoinSessionRequestHandler
{

    private static final Logger log = Logger.getLogger(JoinSessionRequestHandler.class);

    private final ISarosSessionManager sessionManager;

    private final ITransmitter transmitter;

    private final IReceiver receiver;

    private final IPreferenceStore preferenceStore;

    private final PacketListener joinSessionRequestListener = new PacketListener()
    {

        @Override
        public void processPacket(final Packet packet)
        {
            ThreadUtils.runSafeSync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    handleInvitationRequest(new JID(packet.getFrom()),
                            JoinSessionRequestExtension.PROVIDER.getPayload(packet));
                }
            });
        }
    };

    public JoinSessionRequestHandler(ISarosSessionManager sessionManager,
            ITransmitter transmitter, IReceiver receiver,
            IPreferenceStore preferenceStore)
    {
        this.sessionManager = sessionManager;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.preferenceStore = preferenceStore;

        if (Boolean.getBoolean("de.fu_berlin.inf.dpp.server.SUPPORTED"))
        {
            this.receiver.addPacketListener(joinSessionRequestListener,
                    JoinSessionRequestExtension.PROVIDER.getPacketFilter());
        }
    }

    private void handleInvitationRequest(JID from,
            JoinSessionRequestExtension extension)
    {

        ISarosSession session = sessionManager.getSarosSession();

        if (session != null && !session.isHost())
        {
            return;
        }

        if (!preferenceStore.getBoolean(PreferenceConstants.SERVER_ACTIVATED)
                || (session != null && extension.isNewSessionRequested())
                || (session == null && !extension.isNewSessionRequested()))
        {
            sendRejection(from);
            return;
        }

        List<JID> list = Collections.singletonList(from);

        // TODO remove calls to CollaborationUtils
//        if (extension.isNewSessionRequested()) {
//            CollaborationUtils.startSession(new ArrayList<IResource>(), list);
//        } else {
//            CollaborationUtils.addContactsToSession(list);
//        }
    }

    private void sendRejection(JID to)
    {
        transmitter.sendMessageToUser(to, JoinSessionRejectedExtension.PROVIDER
                .create(new JoinSessionRejectedExtension()));
    }
}
