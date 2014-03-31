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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias(/* SessionNegotiationOffering */"SNOF")
public class InvitationOfferingExtension extends InvitationExtension
{

    public static final Provider PROVIDER = new Provider();

    @XStreamAlias("sid")
    @XStreamAsAttribute
    private String sessionID;

    @XStreamAlias("version")
    private String version;

    @XStreamAlias("description")
    private String description;

    public InvitationOfferingExtension(String invitationID, String sessionID,
            String version, String description)
    {
        super(invitationID);

        this.sessionID = sessionID;
        this.version = version;
        this.description = description;
    }

    /**
     * Returns the remote session ID of the inviter.
     *
     * @return
     */
    public String getSessionID()
    {
        return sessionID;
    }

    /**
     * Returns the remote version of the inviter.
     *
     * @return
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns a description why this invitation was offered.
     *
     * @return a user generated description or <code>null</code> if no
     *         description is available
     */
    public String getDescription()
    {
        return description;
    }

    public static class Provider extends
            InvitationExtension.Provider<InvitationOfferingExtension>
    {

        private Provider()
        {
            super("snof", InvitationOfferingExtension.class);
        }
    }
}
