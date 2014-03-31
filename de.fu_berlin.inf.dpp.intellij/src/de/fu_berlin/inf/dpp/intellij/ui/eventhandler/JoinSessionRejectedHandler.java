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

package de.fu_berlin.inf.dpp.intellij.ui.eventhandler;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.27
 * Time: 09.29
 */

import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinSessionRejectedExtension;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;


public final class JoinSessionRejectedHandler
{

    private static final Logger log = Logger.getLogger(JoinSessionRejectedHandler.class);

    private final IReceiver receiver;

    private final PacketListener joinSessionRejectedListener = new PacketListener()
    {

        @Override
        public void processPacket(final Packet packet)
        {
            ThreadUtils.runSafeSync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    handleRejection(new JID(packet.getFrom()),
                            JoinSessionRejectedExtension.PROVIDER
                                    .getPayload(packet));
                }
            });
        }
    };

    public JoinSessionRejectedHandler(IReceiver receiver)
    {
        this.receiver = receiver;
        this.receiver.addPacketListener(joinSessionRejectedListener,
                JoinSessionRejectedExtension.PROVIDER.getPacketFilter());

    }

    private void handleRejection(JID from,
            JoinSessionRejectedExtension extension)
    {

        String name = XMPPUtils.getNickname(null, from);

        if (name == null)
        {
            name = from.getBase();
        }

        //todo
//        DialogUtils.openInformationMessageDialog(SWTUtils.getShell(),
//                "Join Session Request Rejected",
//                "Your request to join the session of " + name + " was rejected.");
    }
}

