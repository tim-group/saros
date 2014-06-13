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

package de.fu_berlin.inf.dpp.intellij.ui.eclipse;

import de.fu_berlin.inf.dpp.core.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.core.ui.IWizard;
import de.fu_berlin.inf.dpp.versioning.VersionManager;

import java.awt.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-11
 * Time: 17:22
 */

public class ShowDescriptionPage implements IWizard
{
    private boolean blockOnOpen;
    private boolean helpAvailable;
    private VersionManager manager;
    private IncomingSessionNegotiation incomingSession;

    public ShowDescriptionPage(VersionManager manager, IncomingSessionNegotiation incomingSession)
    {
        this.manager = manager;
        this.incomingSession = incomingSession;
    }

    public void createControl(Composite pageContainer)
    {
        //todo
        System.out.println("ShowDescriptionPage.createControl //todo");
    }

    @Override
    public void setBlockOnOpen(boolean blockOnOpen)
    {
         this.blockOnOpen = blockOnOpen;
    }

    @Override
    public void setHelpAvailable(boolean helpAvailable)
    {
        this.helpAvailable = helpAvailable;
    }
}
