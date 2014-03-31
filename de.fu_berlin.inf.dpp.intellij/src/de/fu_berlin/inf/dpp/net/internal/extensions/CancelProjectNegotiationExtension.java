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

public class CancelProjectNegotiationExtension extends
        SarosSessionPacketExtension
{

    public static final Provider PROVIDER = new Provider();

    @XStreamAlias("error")
    private String errorMessage;

    public CancelProjectNegotiationExtension(String sessionID,
            String errorMessage)
    {
        super(sessionID);
        if ((errorMessage != null) && (errorMessage.length() > 0))
        {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Returns the error message for this cancellation.
     *
     * @return the error message or <code>null</code> if the remote contact
     *         cancelled the project negotiation manually
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /* *
     *
     * @JTourBusStop 4, Creating custom network messages, Creating the provider:
     *
     * Each packet extension needs a provider so that the marshalled content
     * (XML output) can correctly unmarshalled again.
     *
     * Please use the exact layout as presented in this class. Create a public
     * static final field named PROVIDER. Put this field to the top of the class
     * and put the provider class itself at the bottom of the class.
     *
     * As you see the first argument in the call to the super constructor is the
     * XML element name. IMPORTANT: our logic does not check for correct XML
     * syntax so YOU have to make sure that the name is a valid XML tag.
     *
     * The element name has nothing to do with the XStream alias and can have a
     * completely different name although it MUST be unique among all other
     * packet extensions !
     *
     * The second argument is a var-arg. You must ensure that all classes of the
     * fields that are going to be marshalled are passed into the constructor.
     * Failing to do so will result in XStream annotations that are not
     * processed and so the XML output of the marshalling will not be the same
     * as what you would expected !
     */

    public static class Provider extends
            SarosSessionPacketExtension.Provider<CancelProjectNegotiationExtension>
    {
        private Provider()
        {
            super("pncl", CancelProjectNegotiationExtension.class);
        }
    }
}
