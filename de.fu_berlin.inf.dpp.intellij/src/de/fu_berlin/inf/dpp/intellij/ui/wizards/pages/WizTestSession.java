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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.Wizard;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPage;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-14
 * Time: 08:39
 */

public class WizTestSession
{


   public static void main(String[] args)
    {

        Wizard wiz = new Wizard("Session Invitation");
      //  wiz.getNavigationPanel().setBackButton(null);

        wiz.setHeadPanel(new HeaderPanel("Session invitation",
                "You have been invited to join Saros session. When accepting the invitation by pressing Accept, this dialog will close, the project invitation negotiated in the background and new wizard will open"));

       // wiz.registerPage(new SessionAcceptPage());
        InfoPage infoPage = new InfoPage("info");
        infoPage.addText("raimis@saros-con.impl.com"+ " has invited you to a Saros session with the currently shared project(s)");
        infoPage.addText("Project: test project, Files 7, Size 1,55 MB");

        wiz.registerPage(infoPage);
        wiz.registerPage(new ProgressPage());

        wiz.create();

    }
}
