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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import de.fu_berlin.inf.dpp.filesystem.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 15.37
 */

public class ProjectIntl implements IProject
{
    private Project project;
    private ProjectManager projectManager = ProjectManager.getInstance();
    private LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    private PsiManager psiManager;

    public ProjectIntl(Project project)
    {
        this.project = project;
        this.psiManager = PsiManager.getInstance(this.project);
    }

    public ProjectIntl(String name) throws Exception
    {
        this.project = projectManager.loadAndOpenProject(name);
        this.psiManager = PsiManager.getInstance(this.project);
    }

    public File getBasePath()
    {
        return new File(project.getBasePath());
    }

    @Override
    public void accept(IResourceVisitor visitor, int resource, int container)
    {
        //todo: Implement it
        System.out.println("ProjectIntl.next //todo");
    }


    @Override
    public IFile getFile(String name)
    {
        return new FileIntl(this, name);
    }

    @Override
    public IFile getFile(IPath path)
    {
        return getFile(path.toPortableString());
    }

    @Override
    public IFolder getFolder(String name)
    {
        return new FolderIntl(this, name);
    }

    @Override
    public IFolder getFolder(IPath path)
    {
        return getFolder(path.toPortableString());
    }

    @Override
    public boolean isOpen()
    {
        return project.isOpen();
    }

    @Override
    public void open()
    {
        try
        {
            projectManager.loadAndOpenProject(project.getName()); //todo
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean exists(IPath path)
    {
        return localFileSystem.exists(path.<VirtualFile>getAdapter());
    }

    @Override
    public IResource[] members()
    {
        return members(NONE);
    }

    @Override
    public IResource[] members(int memberFlags)
    {
        List<IResource> list = new ArrayList<IResource>();

        PsiDirectory dir = psiManager.findDirectory(project.getBaseDir());
        addChildren(list, dir, memberFlags);

        return list.toArray(new IResource[]{});

    }


    private void addChildren(List<IResource> list, PsiDirectory dir, int memberFlags)
    {
        for (PsiDirectory res : dir.getSubdirectories())
        {
            if (memberFlags == FOLDER || memberFlags == NONE)
            {
                FolderIntl folder = new FolderIntl(this, res);
                list.add(folder);

                addChildren(list, res, memberFlags);
            }
        }

        if (memberFlags == FILE || memberFlags == NONE)
        {
            for (PsiFile res : dir.getFiles())
            {
                FileIntl file = new FileIntl(this, res);
                list.add(file);
            }
        }
    }

    private String defaultCharset;

    @Override
    public String getDefaultCharset() throws IOException
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
        return project != null;
    }


    @Override
    public IPath getFullPath()
    {
        return new PathIntl(project.getBasePath());
    }


    @Override
    public String getName()
    {
        return project.getName();
    }


    public void setProject(Project project)
    {
        this.project = project;
    }

    @Override
    public IProject getProject()
    {
        return this;
    }

    @Override
    public IPath getProjectRelativePath()
    {
        return new PathIntl(project.getProjectFilePath());
    }

    @Override
    public int getType()
    {
        return IResource.PROJECT;
    }

    @Override
    public boolean isAccessible()
    {
        return project.isOpen();
    }


    @Override
    public boolean isDerived(boolean checkAncestors)
    {
        return isDerived();
    }

    @Override
    public boolean isDerived()
    {
        return parent != null;
    }

    @Override
    public void refreshLocal()
    {
        projectManager.reloadProject(project);
    }


    @Override
    public void delete(int updateFlags) throws IOException
    {

        FileUtils.deleteDirectory(getBasePath());
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException
    {
        getBasePath().renameTo(destination.toFile());
    }

    private IResourceAttributes attributes = new FileResourceAttributes(null);

    @Override
    public IResourceAttributes getResourceAttributes()
    {
        return attributes;
    }

    @Override
    public void setResourceAttributes(IResourceAttributes attributes)
    {
        this.attributes = attributes;
    }

    @Override
    public URI getLocationURI()
    {
        try
        {
            return new URI(project.getBasePath());
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz)
    {
        return this;
    }

    @Override
    public IPath getLocation()
    {
        return new PathIntl(project.getBasePath());
    }

    @Override
    public IResource findMember(IPath path)
    {
        return null;
    }

    private IContainer parent;

    @Override
    public IContainer getParent()
    {
        return parent;
    }

    public void setParent(IContainer parent)
    {
        this.parent = parent;
    }

    @Override
    public File toFile()
    {
        return new File(project.getBasePath());
    }


    public String toString()
    {
        return super.toString() + " [" + project.getName() + "]";
    }

    @Override
    public int hashCode()
    {
        return project.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ProjectIntl))
        {
            return false;
        }

        ProjectIntl other = (ProjectIntl) obj;

        return this.project.equals(other.project);
    }

}
