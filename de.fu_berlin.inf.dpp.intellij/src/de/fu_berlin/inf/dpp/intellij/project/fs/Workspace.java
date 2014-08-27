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
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.intellij.project.FileSystemChangeListener;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Workspace implements IWorkspace {
    public static final Logger LOG = Logger.getLogger(Workspace.class);

    private LocalFileSystem fileSystem;

    private Project project;

    public Workspace(Project project) {
        this.project = project;
        fileSystem = LocalFileSystem.getInstance();
        fileSystem.addRootToWatch(project.getBasePath(), true);
    }

    @Override public void run(IWorkspaceRunnable procedure) throws IOException {
        procedure.run(new NullProgressMonitor());
    }

    @Override
    public IProject getProject(String projectName) {
        return new ProjectImp(project, projectName);
    }

    /**
     * Returns a handle to the project for the given path.
     */
    public ProjectImp getProjectForPath(String path) {

        if (!path.startsWith(project.getBasePath())) {
            LOG.error("tried to access project outside workspace: " + path);
            return null;
        }

        LOG.info("STARTS WITH SEPARATOR OR NOT?: " + project.getBasePath());
        String relativePath = path.substring(project.getBasePath().length()).toLowerCase();
        if (relativePath.startsWith(File.separator)) {
            relativePath = relativePath.substring(1);
        }

        String projectName = new PathImp(relativePath).segment(0);
        return new ProjectImp(project, projectName);
    }

    @Override
    public IPath getLocation() {
        return new PathImp(project.getBasePath());
    }

    public void addResourceListener(FileSystemChangeListener listener) {
        listener.setWorkspace(this);
        fileSystem.addVirtualFileListener(listener);
    }

    public void removeResourceListener(FileSystemChangeListener listener) {
        listener.setWorkspace(this);
        fileSystem.removeVirtualFileListener(listener);
    }
}
