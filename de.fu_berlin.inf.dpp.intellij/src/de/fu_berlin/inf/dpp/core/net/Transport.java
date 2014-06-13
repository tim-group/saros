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

package de.fu_berlin.inf.dpp.core.net;



import de.fu_berlin.inf.dpp.net.ConnectionMode;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnectionListener;
import de.fu_berlin.inf.dpp.net.internal.ITransport;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.jivesoftware.smack.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Transport implements ITransport {

    private List<ChannelConnection> establishedConnections = new ArrayList<ChannelConnection>();

    private IByteStreamConnectionListener listener;

    private ConnectionMode mode;

    private String connectionID;

    public Transport(ConnectionMode mode) {
        this.mode = mode;
    }

    @Override
    public synchronized IByteStreamConnection connect(
        String connectionIdentifier, JID peer) throws IOException,
        InterruptedException {

        connectionID = connectionIdentifier;

        ChannelConnection connection = new ChannelConnection(peer, mode,
            listener);

        establishedConnections.add(connection);
        return connection;
    }

    public synchronized void announceIncomingRequest(JID peer) {
        ChannelConnection connection = new ChannelConnection(peer, mode,
            listener);

        establishedConnections.add(connection);
        listener.connectionChanged(connectionID, peer, connection, true);
    }

    @Override
    public void initialize(Connection connection,
        IByteStreamConnectionListener listener) {
        this.listener = listener;

    }

    @Override
    public void uninitialize() {
        this.listener = null;

    }

    public synchronized List<ChannelConnection> getEstablishedConnections() {
        return establishedConnections;
    }
}