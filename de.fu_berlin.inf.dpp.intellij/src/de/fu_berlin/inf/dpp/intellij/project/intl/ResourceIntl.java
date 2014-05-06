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

package de.fu_berlin.inf.dpp.intellij.project.intl;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;

import java.io.File;
import java.net.URI;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.4
 * Time: 13.42
 */

public abstract class ResourceIntl implements IResource
{
    public static final String DEFAULT_CHARSET = "utf8";

    protected ProjectIntl project;
    protected File file;
    protected VirtualFile virtualFile;
    protected LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

    private String defaultCharset = DEFAULT_CHARSET;

    private IResourceAttributes attributes;
    private boolean isDerived = false;

    protected ResourceIntl(ProjectIntl project,VirtualFile virtualFile)
    {
        this.project = project;
        this.virtualFile = virtualFile;
        this.file = new File(virtualFile.getPath());
    }

    protected ResourceIntl(ProjectIntl project, File file)
    {
        this.project = project;
        this.file = file;
        this.virtualFile = localFileSystem.findFileByIoFile(file);
    }

    protected ResourceIntl(ProjectIntl project, String path)
    {
        this.project = project;
        this.file = new File(path);
        this.virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
    }

    public String getDefaultCharset()
    {
        return defaultCharset;
    }

    public void setDefaultCharset(String defaultCharset)
    {
        this.defaultCharset = defaultCharset;
    }

    @Override
    public boolean exists()
    {
        return file.exists();
    }

    @Override
    public IPath getFullPath()
    {
        return new PathIntl(file.getAbsoluteFile());
    }

    @Override
    public String getName()
    {
        return file.getName();
    }

    @Override
    public IContainer getParent()
    {
        return file == null || file.getParentFile() == null ? null : new FolderIntl(project,file.getParentFile());
    }

    public ProjectIntl getProject()
    {
        return project;
    }

    public void setProject(ProjectIntl project)
    {
        this.project = project;
    }


    @Override
    public IPath getProjectRelativePath()
    {
        if (project == null)
        {
            return new PathIntl(file);
        }
        else
        {
            String prjPath = project.getFullPath().toFile().getAbsolutePath();
            String myPath = file.getAbsolutePath();
            myPath = myPath.substring(prjPath.length());

            return new PathIntl(new File(myPath));
        }

    }

    @Override
    public int getType()
    {
        return NONE;
    }

    @Override
    public boolean isAccessible()
    {
        return file.canRead();
    }

    @Override
    public boolean isDerived(boolean checkAncestors)
    {
        return isDerived(); //todo.
    }

    @Override
    public boolean isDerived()
    {
        return isDerived;
    }


    public void setDerived(boolean derived)
    {
        isDerived = derived;
    }


    public IResourceAttributes getResourceAttributes()
    {
        return attributes;
    }

    public void setResourceAttributes(IResourceAttributes attributes)
    {
        this.attributes = attributes;
    }

    @Override
    public URI getLocationURI()
    {
        return file.toURI();
    }

    public VirtualFile getVirtualFile()
    {
        return virtualFile;
    }



}
