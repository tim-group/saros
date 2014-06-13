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

package de.fu_berlin.inf.dpp.core.net.util;

import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.picocontainer.annotations.Inject;

import java.util.Iterator;

/**
 * Utility class for classic {@link org.jivesoftware.smack.Roster} operations
 * 
 * @author bkahlert
 */
// FIXME due to the hack this class cannot currently moved to the Saros core
public class RosterUtils {
    public final static String RESOURCE = "SarosEclipse"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(RosterUtils.class);

    @Inject
    private static XMPPConnectionService defaultConnectionService;

    /*
     * HACK this should be initialized in a better way and removed if resolving
     * nicknames is removed from the User class
     */

    static {
         SarosPluginContext.initComponent(new RosterUtils()); //todo
    }

    private RosterUtils() {
        // no public instantiation allowed
    }

    /**
     * @param connectionService
     *            network component that should be used to resolve the nickname
     *            or <code>null</code> to use the default one
     * @param jid
     *            the JID to resolve the nickname for
     * @return The nickname associated with the given JID in the current roster
     *         or null if the current roster is not available or the nickname
     *         has not been set.
     */
    public static String getNickname(XMPPConnectionService connectionService,
        JID jid) {

        if (connectionService == null) {
            connectionService = defaultConnectionService;
        }

        if (connectionService == null) {
            return null;
        }

        Connection connection = connectionService.getConnection();
        if (connection == null) {
            return null;
        }

        Roster roster = connection.getRoster();
        if (roster == null) {
            return null;
        }

        RosterEntry entry = roster.getEntry(jid.getBase());
        if (entry == null) {
            return null;
        }

        String nickName = entry.getName();
        if (nickName != null && nickName.trim().length() > 0) {
            return nickName;
        }
        return null;
    }

    public static String getDisplayableName(RosterEntry entry) {
        String nickName = entry.getName();
        if (nickName != null && nickName.trim().length() > 0) {
            return nickName.trim();
        }
        return entry.getUser();
    }

    /**
     * Creates the given account on the given XMPP server.
     * 
     * @param server
     *            the server on which to create the account
     * @param username
     *            for the new account
     * @param password
     *            for the new account
     * @return <code>null</code> if the account was registered, otherwise a
     *         {@link org.jivesoftware.smack.packet.Registration description} is
     *         returned which may containing additional information on how to
     *         register an account on the given XMPP server or an error code
     * @throws org.jivesoftware.smack.XMPPException
     *             exception that occurs while registering
     * @blocking
     * @see org.jivesoftware.smack.packet.Registration#getError()
     */
    public static Registration createAccount(String server, String username,
        String password) throws XMPPException {

        Connection connection = new XMPPConnection(server);

        try {
            connection.connect();

            Registration registration = getRegistrationInfo(connection,
                username);

            if (registration != null) {

                // no in band registration
                if (registration.getError() != null) {
                    return registration;
                }

                // already registered
                if (registration.getAttributes().containsKey("registered")) {
                    return registration;
                }

                // redirect
                if (registration.getAttributes().size() == 1
                    && registration.getAttributes().containsKey("instructions")) {
                    return registration;
                }
            }

            AccountManager manager = connection.getAccountManager();
            manager.createAccount(username, password);
        } finally {
            connection.disconnect();
        }

        return null;
    }

    /**
     * Removes given contact from the {@link Roster}.
     * 
     * @param rosterEntry
     *            the contact that is to be removed
     * @throws XMPPException
     *             is thrown if no connection is established.
     * @blocking
     */
    public static void removeFromRoster(Connection connection,
        RosterEntry rosterEntry) throws XMPPException {
        if (!connection.isConnected()) {
            throw new XMPPException("Not connected");
        }
        connection.getRoster().removeEntry(rosterEntry);
    }

    /**
     * Returns whether the given JID can be found on the server.
     * 
     * @param connection
     * @throws XMPPException
     *             if the service discovery failed
     * @blocking
     */
    public static boolean isJIDonServer(Connection connection, JID jid)
        throws XMPPException {

        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(connection);

        boolean discovered = sdm.discoverInfo(jid.getRAW()).getIdentities()
            .hasNext();

        if (!discovered && jid.isBareJID()) {
            discovered = sdm.discoverInfo(jid.getBase() + "/" + RESOURCE)
                .getIdentities().hasNext();
        }

        return discovered;
    }

    /**
     * Retrieve XMPP Registration information from a server.
     * <p/>
     * This implementation reuses code from Smack but also sets the from element
     * of the IQ-Packet so that the server could reply with information that the
     * account already exists as given by XEP-0077.
     * <p/>
     * To see what additional information can be queried from the registration
     * object, refer to the XEP directly:
     * <p/>
     * http://xmpp.org/extensions/xep-0077.html
     */
    public static synchronized Registration getRegistrationInfo(
        Connection connection, String toRegister) throws XMPPException {
        Registration reg = new Registration();
        reg.setTo(connection.getServiceName());
        reg.setFrom(toRegister);
        PacketFilter filter = new AndFilter(new PacketIDFilter(
            reg.getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector = connection.createPacketCollector(filter);

        final IQ result;

        try {
            connection.sendPacket(reg);
            result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());

        } finally {
            collector.cancel();
        }

        if (result == null) {
            throw new XMPPException("No response from server.");
        } else if (result.getType() == IQ.Type.ERROR) {
            throw new XMPPException(result.getError());
        } else {
            return (Registration) result;
        }
    }

    /**
     * Returns the service for a user directory. The user directory can be used
     * to perform search queries.
     * 
     * @param connection
     *            the current XMPP connection
     * @param service
     *            a service, normally the domain of a XMPP server
     * @return the service for the user directory or <code>null</code> if it
     *         could not be determined
     * @See {@link org.jivesoftware.smackx.search.UserSearch#getSearchForm(Connection con, String searchService)}
     */
    public static String getUserDirectoryService(Connection connection,
        String service) {

        ServiceDiscoveryManager manager = ServiceDiscoveryManager
            .getInstanceFor(connection);

        DiscoverItems items;

        try {
            items = manager.discoverItems(service);
        } catch (XMPPException e) {
            log.error("discovery for service '" + service + "' failed", e);
            return null;
        }

        Iterator<DiscoverItems.Item> iter = items.getItems();
        while (iter.hasNext()) {
            DiscoverItems.Item item = iter.next();
            try {
                Iterator<DiscoverInfo.Identity> identities = manager
                    .discoverInfo(item.getEntityID()).getIdentities();
                while (identities.hasNext()) {
                    DiscoverInfo.Identity identity = identities.next();
                    if ("user".equalsIgnoreCase(identity.getType())) {
                        return item.getEntityID();
                    }
                }
            } catch (XMPPException e) {
                log.warn("could not query identity: " + item.getEntityID(), e);
            }
        }

        iter = items.getItems();

        // make a good guess
        while (iter.hasNext()) {
            DiscoverItems.Item item = iter.next();

            String entityID = item.getEntityID();

            if (entityID == null) {
                continue;
            }

            if (entityID.startsWith("vjud.") || entityID.startsWith("search.")
                || entityID.startsWith("users.") || entityID.startsWith("jud.")
                || entityID.startsWith("id.")) {
                return entityID;
            }
        }

        return null;
    }

    /**
     * Returns the service for multiuser chat.
     * 
     * @param connection
     *            the current XMPP connection
     * @param service
     *            a service, normally the domain of a XMPP server
     * @return the service for the multiuser chat or <code>null</code> if it
     *         could not be determined
     */
    public static String getMultiUserChatService(Connection connection,
        String service) {

        ServiceDiscoveryManager manager = ServiceDiscoveryManager
            .getInstanceFor(connection);

        DiscoverItems items;

        try {
            items = manager.discoverItems(service);
        } catch (XMPPException e) {
            log.error("discovery for service '" + service + "' failed", e);
            return null;
        }

        Iterator<DiscoverItems.Item> iter = items.getItems();
        while (iter.hasNext()) {
            DiscoverItems.Item item = iter.next();
            try {
                Iterator<DiscoverInfo.Identity> identities = manager
                    .discoverInfo(item.getEntityID()).getIdentities();
                while (identities.hasNext()) {
                    DiscoverInfo.Identity identity = identities.next();
                    if ("text".equalsIgnoreCase(identity.getType())
                        && "conference"
                            .equalsIgnoreCase(identity.getCategory())) {
                        return item.getEntityID();
                    }
                }
            } catch (XMPPException e) {
                log.warn("could not query identity: " + item.getEntityID(), e);
            }
        }

        return null;
    }
}
