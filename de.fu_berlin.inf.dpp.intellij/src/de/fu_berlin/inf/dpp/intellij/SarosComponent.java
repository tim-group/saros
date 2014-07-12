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

package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.core.Saros;
import org.apache.log4j.PropertyConfigurator;
import org.jetbrains.annotations.NotNull;


/**
 * Component that is initalized when a project is loaded. It initializes Saros.
 */
public class SarosComponent implements com.intellij.openapi.components.ProjectComponent {


    public SarosComponent(Project project) {
        PropertyConfigurator.configure("/home/holger/code/saros-raimondas/de.fu_berlin.inf.dpp.intellij/src/log4j.properties");  //todo

        Saros.create(project);
        System.out.println(Saros.getInstance());
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Saros";
    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }
}
