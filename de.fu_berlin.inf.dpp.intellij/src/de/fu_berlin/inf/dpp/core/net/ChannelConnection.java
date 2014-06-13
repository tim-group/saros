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
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

import java.io.IOException;

//todo: used in Transport only, remove later
@Deprecated
public class ChannelConnection implements IByteStreamConnection {

    private JID to;
    private ConnectionMode mode;
    private IByteStreamConnectionListener listener;
    private volatile boolean closed;
    private volatile int sendPackets;

    public ChannelConnection(JID to, ConnectionMode mode,
        IByteStreamConnectionListener listener) {
        this.to = to;
        this.mode = mode;
        this.listener = listener;
    }

    @Override
    public JID getPeer() {
        return to;
    }

    @Override
    public void close() {
        closed = true;
        listener.connectionClosed(/* FIMXE */null, to, this);
    }

    @Override
    public boolean isConnected() {
        return !closed;
    }

    @Override
    public void send(TransferDescription data, byte[] content)
        throws IOException {
        sendPackets++;
    }

    @Override
    public ConnectionMode getMode() {
        return mode;
    }

    public int getSendPacketsCount() {
        return sendPackets;
    }

    @Override
    public String getConnectionID() {
        return null;
    }

    @Override
    public void initialize() {
        // NOP
    }
}
