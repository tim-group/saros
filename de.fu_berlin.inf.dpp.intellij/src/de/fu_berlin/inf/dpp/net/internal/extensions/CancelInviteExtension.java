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

public class CancelInviteExtension extends InvitationExtension
{

    public static final Provider PROVIDER = new Provider();

    @XStreamAlias("error")
    private String errorMessage;

    public CancelInviteExtension(String invitationID, String errorMessage)
    {
        super(invitationID);
        if ((errorMessage != null) && (errorMessage.length() > 0))
        {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Returns the error message for this cancellation.
     *
     * @return the error message or <code>null</code> if the remote contact
     *         cancelled the invitation manually
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    public static class Provider extends
            InvitationExtension.Provider<CancelInviteExtension>
    {
        private Provider()
        {
            super("sncl", CancelInviteExtension.class);
        }
    }
}