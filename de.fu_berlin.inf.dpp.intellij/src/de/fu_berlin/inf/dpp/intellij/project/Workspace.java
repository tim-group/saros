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

package de.fu_berlin.inf.dpp.intellij.project;

import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.project.ISchedulingRoot;
import de.fu_berlin.inf.dpp.core.project.WorkspaceDescription;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceDescription;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;

import java.io.IOException;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 11.03
 * //todo
 */

public class Workspace implements IWorkspace
{
    @Override
    public void run(IWorkspaceRunnable deleteProcedure, IProgressMonitor monitor) throws OperationCanceledException, IOException
    {
        System.out.println("Workspace.run");
    }

    @Override
    public void run(IWorkspaceRunnable deleteProcedure, ISchedulingRoot root, int mode, IProgressMonitor monitor)
    {
        System.out.println("Workspace.run");
    }

    @Override
    public ISchedulingRoot getRoot()
    {
        System.out.println("Workspace.getRoot");
        return new SchedulingRoot(); //todo
    }

    @Override
    public IWorkspaceDescription getDescription()
    {
        System.out.println("Workspace.getDescription");
        return new WorkspaceDescription(); //todo
    }

    @Override
    public void setDescription(IWorkspaceDescription description) throws IOException
    {
        System.out.println("Workspace.setDescription");
    }
}
