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

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;


public class BlockableTransport extends Transport {

    private CountDownLatch acknowledge;

    private CountDownLatch proceed;

    private volatile boolean isConnecting;

    private Set<JID> jidsToIgnore;

    public BlockableTransport(Set<JID> jidsToIgnore, NetTransferMode mode,
        CountDownLatch acknowledge, CountDownLatch proceed) {
        super(mode);
        this.acknowledge = acknowledge;
        this.proceed = proceed;
        this.jidsToIgnore = jidsToIgnore;
    }

    @Override
    public IByteStreamConnection connect(String connectionIdentifier, JID peer)
        throws IOException, InterruptedException {

        if (jidsToIgnore.contains(peer)) {
            return super.connect(connectionIdentifier, peer);
        }

        synchronized (this) {
            if (isConnecting) {
                throw new IllegalStateException(
                    "connect must not be called concurrently");
            }
            isConnecting = true;
        }

        acknowledge.countDown();
        proceed.await();
        IByteStreamConnection connection = super.connect(connectionIdentifier,
            peer);
        isConnecting = false;
        return connection;
    }
}
