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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.core.project.ISchedulingRoot;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceDescription;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.intellij.project.events.FileSystemChangeListener;
import org.apache.log4j.Logger;

import java.io.File;
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
    public static final Logger log = Logger.getLogger(Workspace.class);
    private SchedulingRoot root;

    private LocalFileSystem fileSystem = LocalFileSystem.getInstance();

    private File path;
    private Project project;

    /**
     * @deprecated use for testing only
     */
    public Workspace()
    {
    }

    public Workspace(File basePath)
    {
        initPath(basePath);
    }

    public Workspace(String basePath)
    {
        this(new File(basePath));
    }

    public Workspace(Project project)
    {
        this.project = project;
        //initPath(new File(project.getBasePath()));
        createWorkSpace(new File(project.getBasePath()));
    }

    private IWorkspaceDescription description = new WorkspaceDescription();

    @Override
    public void run(final IWorkspaceRunnable procedure, IProgressMonitor monitor) throws OperationCanceledException, IOException
    {
        if (monitor == null)
        {
            monitor = new NullProgressMonitor();
        }

        try
        {
            procedure.run(monitor);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run(IWorkspaceRunnable procedure, ISchedulingRoot root, int mode, IProgressMonitor monitor)
    {

        if (monitor == null)
        {
            monitor = new NullProgressMonitor();
        }

        try
        {
            procedure.run(monitor);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ISchedulingRoot getRoot()
    {
        if (root == null)
        {
            throw new RuntimeException("Workspace not initialized!");
        }

        return root;
    }

    public IWorkspaceDescription getDescription()
    {
        return description;
    }

    public void setDescription(IWorkspaceDescription description)
    {
        this.description = description;
    }

    /**
     * @param path
     * @deprecated use for tests only
     */
    public void createWorkSpace(File path)
    {
        this.root = new SchedulingRoot(path);
        this.path = path;

        log.info("Add workspace " + path.getAbsolutePath());
        for (File prj : path.listFiles())
        {
            if (prj.isDirectory())
            {
                root.addProject(prj.getName(), prj);
            }
        }
    }


    @Override
    public IPathFactory getPathFactory()
    {
        return new PathFactory();
    }

    public File getPath()
    {
        return path;
    }

    protected void initPath(File path)
    {
        this.root = new SchedulingRoot(path);
        this.path = path;
    }

    @Override
    public void addResourceListener(FileSystemChangeListener listener)
    {
        listener.setWorkspace(this);
        fileSystem.addVirtualFileListener(listener);
    }

    @Override
    public void removeResourceListener(FileSystemChangeListener listener)
    {
        fileSystem.removeVirtualFileListener(listener);
    }

}
