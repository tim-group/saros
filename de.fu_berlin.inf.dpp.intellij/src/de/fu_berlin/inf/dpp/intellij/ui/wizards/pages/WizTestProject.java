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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.Wizard;

import java.io.File;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-14
 * Time: 08:39
 */

public class WizTestProject
{


   public static void main(String[] args)
    {

        Wizard wiz = new Wizard("Add Projects");
      //  wiz.getNavigationPanel().setBackButton(null);

        wiz.setHeadPanel(new HeaderPanel("Select local project",""));

       // wiz.registerPage(new SessionAcceptPage());
        SelectProjectPage infoPage = new SelectProjectPage("SelectProject");
        infoPage.setNewProjectName("TestasProjektas1");
        infoPage.setProjectName("Testas Proj");
        infoPage.setProjectBase(new File("").getAbsolutePath());
        infoPage.create();


        wiz.registerPage(infoPage);

        InfoWithProgressPage info1 = new InfoWithProgressPage();

        //info1.setFontColor(Color.red);
        info1.setTitle( "WARNING: Local file changes will be overwritten:");
        info1.create();


        wiz.registerPage(info1);

        IProgressMonitor monitor = info1.getProgressMonitor(true, false);

        wiz.registerPage(new ProgressPage());

        wiz.create();

        info1.addLine("vienas");
        info1.addLine("du");
        info1.addLine("trys");
        info1.addLine("keturi");
        info1.addLine("penki");
        info1.addLine("sesi");
        info1.addLine("septyni");
        info1.addLine("astuoni");
        info1.addLine("devyni");
        info1.addLine("desimt");
        info1.addLine("vienuolika");
        info1.addLine("dvylika");
        info1.addLine("trylika");
        info1.addLine("keturiolika");

    }
}
