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

package de.fu_berlin.inf.dpp.core.context;

import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.core.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.core.communication.chart.muc.negotiation.MUCNegotiationManager;
import de.fu_berlin.inf.dpp.core.editor.colorstorage.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.core.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.core.net.business.CancelInviteHandler;
import de.fu_berlin.inf.dpp.core.net.business.CancelProjectSharingHandler;
import de.fu_berlin.inf.dpp.core.net.business.InvitationHandler;
import de.fu_berlin.inf.dpp.core.net.business.LeaveAndKickHandler;
import de.fu_berlin.inf.dpp.core.observables.*;
import de.fu_berlin.inf.dpp.core.project.internal.ColorNegotiationHook;
import de.fu_berlin.inf.dpp.core.project.internal.FollowingActivitiesManager;
import de.fu_berlin.inf.dpp.core.ui.RemoteProgressManager;
import de.fu_berlin.inf.dpp.core.versioning.VersionManager;
import de.fu_berlin.inf.dpp.intellij.concurrent.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.net.*;
import de.fu_berlin.inf.dpp.net.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.*;
import de.fu_berlin.inf.dpp.net.stun.IStunService;
import de.fu_berlin.inf.dpp.net.stun.internal.StunServiceImpl;
import de.fu_berlin.inf.dpp.net.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPAccess;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPAccessImpl;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPServiceImpl;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import java.util.Arrays;

//import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
//import de.fu_berlin.inf.dpp.communication.SkypeManager;
//import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCNegotiationManager;
//import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
//import de.fu_berlin.inf.dpp.core.editor.colorstorage.ColorIDSetStorage;
//import de.fu_berlin.inf.dpp.core.net.business.CancelInviteHandler;
//import de.fu_berlin.inf.dpp.core.net.business.CancelProjectSharingHandler;
//import de.fu_berlin.inf.dpp.core.net.business.InvitationHandler;
//import de.fu_berlin.inf.dpp.core.net.business.LeaveAndKickHandler;
//import de.fu_berlin.inf.dpp.net.discovery.DiscoveryManager;
//import de.fu_berlin.inf.dpp.core.observables.FileReplacementInProgressObservable;
//import de.fu_berlin.inf.dpp.core.observables.SarosSessionObservable;
//import de.fu_berlin.inf.dpp.core.observables.SessionIDObservable;
//import de.fu_berlin.inf.dpp.core.project.SarosSessionManager;
//import de.fu_berlin.inf.dpp.core.project.internal.FollowingActivitiesManager;

/**
 * This is the basic core factory for Saros. All components that are created by
 * this factory <b>must</b> be working on any platform the application is
 * running on.
 *
 * @author srossbach
 */
public class SarosCoreContextFactory extends AbstractSarosContextFactory
{

    // TODO we must abstract the IPrefenceStore stuff otherwise anything here is
    // broken

    private final Component[] components = new Component[]{

            // Version support ... broken uses Eclipse / OSGi STUFF
            Component.create(VersionManager.class),

            Component.create(MultiUserChatService.class),
            Component.create(SingleUserChatService.class),


            Component.create(XMPPAccountStore.class),
            Component.create(ColorIDSetStorage.class),

            // Invitation hooks
            Component.create(SessionNegotiationHookManager.class),
            Component.create(ColorNegotiationHook.class),
             Component.create(MUCNegotiationManager.class),  //todo

            // Network
            Component.create(DispatchThreadContext.class),

            Component.create(DataTransferManager.class),

            Component.create(DiscoveryManager.class),

            Component.create(BindKey.bindKey(ITransport.class,
                    ISarosContextBindings.IBBTransport.class), IBBTransport.class),

            Component.create(BindKey.bindKey(ITransport.class,
                            ISarosContextBindings.Socks5Transport.class),
                    Socks5Transport.class
            ),

            Component.create(RosterTracker.class),
            Component.create(XMPPConnectionService.class),
            //  Component.create(SkypeManager.class),

            Component.create(IStunService.class, StunServiceImpl.class),

            Component.create(SubscriptionHandler.class),

            Component.create(IUPnPService.class, UPnPServiceImpl.class),
            Component.create(IUPnPAccess.class, UPnPAccessImpl.class),
            Component.create(IReceiver.class, XMPPReceiver.class),
            Component.create(ITransmitter.class, XMPPTransmitter.class),

            // Observables
            Component.create(FileReplacementInProgressObservable.class),
            Component.create(InvitationProcessObservable.class),
            Component.create(ProjectNegotiationObservable.class),
            Component.create(IsInconsistentObservable.class),
            Component.create(SessionIDObservable.class),
            Component.create(SarosSessionObservable.class),
            Component.create(AwarenessInformationCollector.class),
            Component.create(FollowingActivitiesManager.class),

            // Handlers
            Component.create(CancelInviteHandler.class),
            Component.create(CancelProjectSharingHandler.class),
            Component.create(InvitationHandler.class),
            Component.create(LeaveAndKickHandler.class),

            Component.create(RemoteProgressManager.class),



    };

    @Override
    public void createComponents(MutablePicoContainer container)
    {
        for (Component component : Arrays.asList(components))
        {

            container.addComponent(component.getBindKey(),
                    component.getImplementation());
        }
    }
}
