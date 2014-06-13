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

package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import org.picocontainer.annotations.Inject;

import java.io.IOException;

public class CreateProjectTask implements IWorkspaceRunnable
{

    private final String name;
    private final IProject base;
    private final IProgressMonitor monitor;

    private IProject project;

    @Inject
    private IWorkspace workspace;

    /**
     * Creates a create project task that can be executed by
     * {@link IWorkspace#run}. The project <b>must not exist</b>.
     *
     * @param name    the name of the new project
     * @param base    project to copy the contents from or <code>null</code> to
     *                create an empty project
     * @param monitor monitor that is used for progress report and cancellation or
     *                <code>null</code> to use the monitor provided by the
     *                {@link #run(IProgressMonitor)} method
     */
    public CreateProjectTask(String name, IProject base,
            IProgressMonitor monitor)
    {

        this.name = name;
        this.base = base;
        this.monitor = monitor;
    }

    public void setWorkspace(IWorkspace workspace)
    {
        this.workspace = workspace;
    }

    /**
     * @return the newly created project or <code>null</code> if it has not been
     *         created yet
     */
    public IProject getProject()
    {
        return project;
    }

    @Override
    public void run(IProgressMonitor monitor) throws IOException
    {
        if (this.monitor != null)
        {
            monitor = this.monitor;
        }

        ISubMonitor subMonitor = monitor.convert(
                "Creating new project... ", base == null ? 2 : 3);

        IWorkspaceRoot workspaceRoot = workspace.getRoot();

        project = workspaceRoot.getProject(name);


       //todo: implement it
        /* try {
            if (project.exists())
                throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                        MessageFormat.format("Project {0} already exists!", name)));

            if (base != null && !base.exists())
                throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                        MessageFormat.format("Project {0} does not exists!", base)));

            if (project.equals(base))
                throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                        MessageFormat.format(
                                "Project {0} is the same as project {1}!", name, base)));

            project.create(subMonitor.newChild(0,
                    SubMonitor.SUPPRESS_ALL_LABELS));

            project
                    .open(subMonitor.newChild(0, SubMonitor.SUPPRESS_ALL_LABELS));

            subMonitor.subTask("refreshing file contents");
            project.refreshLocal(IResource.DEPTH_INFINITE,
                    subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));

            subMonitor.subTask("clearing history");
            project.clearHistory(subMonitor.newChild(1,
                    SubMonitor.SUPPRESS_ALL_LABELS));

            if (base != null) {
                subMonitor.subTask("copying contents from project "
                        + base.getName());
                base.copy(project.getFullPath(), true,
                        subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
            }
        } finally {
            subMonitor.subTask("");
            monitor.done();
        }*/

        //todo
    }
}
