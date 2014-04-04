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

import de.fu_berlin.inf.dpp.filesystem.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.4
 * Time: 13.42
 */

public abstract class AbstractContainer implements IContainer
{
    protected Project project;
    protected File file;
    private String defaultCharset = DEFAULT_CHARSET;

    private IResourceAttributes attributes;
    private boolean isDerived = false;

    protected AbstractContainer(Project project, File file)
    {
        this.project = project;
        this.file = file;
    }

    protected AbstractContainer(File file)
    {
        this.file = file;
    }


    @Override
    public boolean exists(IPath path)
    {
        return new File(path.toString()).exists();
    }

    @Override
    public IResource[] members() throws IOException
    {
        return new IResource[0];
    }

    @Override
    public IResource[] members(int memberFlags) throws IOException
    {
        return new IResource[0];
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
        return new PathImp(file.getAbsoluteFile());
    }

    @Override
    public String getName()
    {
        return file.getName();
    }

    @Override
    public IContainer getParent()
    {
        return new FolderImp(file.getParentFile());
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    @Override
    public IPath getProjectRelativePath()
    {
        if(project ==null)
        {
             return new PathImp(file);
        }
        else
        {
            String prjPath = project.getFullPath().toFile().getAbsolutePath();
            String myPath = file.getAbsolutePath();
            myPath = myPath.substring(prjPath.length());
            
            return new PathImp(new File(myPath));
        }

    }

    @Override
    public abstract int getType();


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

    @Override
    public void refreshLocal() throws IOException
    {
        System.out.println("AbstractContainer.refreshLocal");
        //todo
    }


    @Override
    public abstract void delete(int updateFlags) throws IOException;


    @Override
    public abstract void move(IPath destination, boolean force) throws IOException;


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

    @Override
    public abstract Object getAdapter(Class<? extends IResource> clazz);

}
