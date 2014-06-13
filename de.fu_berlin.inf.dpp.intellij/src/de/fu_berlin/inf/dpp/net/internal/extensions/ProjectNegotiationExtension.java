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

package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class ProjectNegotiationExtension extends
        SarosSessionPacketExtension {

    @XStreamAlias("nid")
    @XStreamAsAttribute
    protected final String negotiationID;

    protected ProjectNegotiationExtension(String sessionID, String negotiationID) {
        super(sessionID);
        this.negotiationID = negotiationID;
    }

    public String getNegotiationID() {
        return negotiationID;
    }

    public abstract static class Provider<T extends ProjectNegotiationExtension>
            extends SarosSessionPacketExtension.Provider<T> {

            public Provider(String elementName, Class<?>... classes) {
            super(elementName, classes);
        }

        /* *
         *
         * @JTourBusStop 5, Creating custom network messages, Extending the
         * packet filter:
         *
         * It might be necessary to extends the packet filter so here is the
         * basic example how to extend it properly.
         */

        public PacketFilter getPacketFilter(final String sessionID,
                final String negotiationID) {

            return new AndFilter(super.getPacketFilter(sessionID),
                    new PacketFilter() {
                        @Override
                        public boolean accept(Packet packet) {
                            ProjectNegotiationExtension extension = getPayload(packet);

                            if (extension == null)
                                return false;

                            return negotiationID.equals(extension
                                    .getNegotiationID());
                        }
                    });
        }
    }
}