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

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;


public class ServerPreferenceHandler {

    private IPreferenceStore preferenceStore;

    private IConnectionListener connectionListener = new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection,
                ConnectionState newState) {

            // Adding the feature while state is CONNECTING would be much
            // better, yet it's not possible since the ServiceDiscoveryManager
            // is not available at that point
            if (ConnectionState.CONNECTED.equals(newState)) {
                if (Boolean.getBoolean("de.fu_berlin.inf.dpp.server.SUPPORTED")) {
                    if (preferenceStore
                            .getBoolean(PreferenceConstants.SERVER_ACTIVATED)) {
                        addServerFeature(connection);
                    } else {
                        removeServerFeature(connection);
                    }
                }
            }
        }
    };

    public ServerPreferenceHandler(XMPPConnectionService connectionService,
            IPreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;

        connectionService.addListener(connectionListener);
    }

    private void addServerFeature(Connection connection) {
        if (connection == null)
            return;

        ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager
                .getInstanceFor(connection);

        if (discoveryManager == null)
            return;

        discoveryManager.addFeature(Saros.NAMESPACE_SERVER);
    }

    private void removeServerFeature(Connection connection) {
        if (connection == null)
            return;

        ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager
                .getInstanceFor(connection);

        if (discoveryManager == null)
            return;

        discoveryManager.removeFeature(Saros.NAMESPACE_SERVER);
    }
}
