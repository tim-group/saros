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
 * Time: 11.57
 */

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.net.subscription.SubscriptionListener;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

/**
 * Handler for accepting or rejecting incoming XMPP subscription requests
 */
public class XMPPAuthorizationHandler
{

    private static final Logger log = Logger
            .getLogger(XMPPAuthorizationHandler.class);

    private final SubscriptionHandler subscriptionHandler;

    private final SubscriptionListener subscriptionListener = new SubscriptionListener()
    {

        @Override
        public void subscriptionRequestReceived(final JID jid)
        {

            ThreadUtils.runSafeSync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    handleAuthorizationRequest(jid);
                }
            });
        }
    };

    public XMPPAuthorizationHandler(
            final SubscriptionHandler subscriptionHandler)
    {
        this.subscriptionHandler = subscriptionHandler;
        this.subscriptionHandler
                .addSubscriptionListener(subscriptionListener);
    }

    private void handleAuthorizationRequest(final JID jid)
    {

        //todo
        boolean accept = true;
        //MessageDialog
//                .openConfirm(
//                        SWTUtils.getShell(),
//                        Messages.SubscriptionManager_incoming_subscription_request_title,
//                        MessageFormat
//                                .format(
//                                        Messages.SubscriptionManager_incoming_subscription_request_message,
//                                        jid.getBareJID()));

        if (accept)
        {
            subscriptionHandler.addSubscription(jid, true);
        }
        else
        {
            subscriptionHandler.removeSubscription(jid);
        }
    }
}
