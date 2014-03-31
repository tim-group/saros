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

import java.io.IOException;
import java.net.URI;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 15.37
 */

public class Project implements IProject, Comparable<Project>
{
    private String name;

    public Project()
    {
    }

    public Project(String name)
    {
        this.name = name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public IResource findMember(IPath path)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IFile getFile(String name)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IFile getFile(IPath path)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IFolder getFolder(String name)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IFolder getFolder(IPath path)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @Override
    public void open() throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean exists(IPath path)
    {
        return true;
    }

    @Override
    public IResource[] members() throws IOException
    {
        return new IResource[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IResource[] members(int memberFlags) throws IOException
    {
        return new IResource[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDefaultCharset() throws IOException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean exists()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IPath getFullPath()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public IContainer getParent()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IProject getProject()
    {
        return this;
    }

    @Override
    public IPath getProjectRelativePath()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getType()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAccessible()
    {
        return true;
    }

    @Override
    public boolean isDerived(boolean checkAncestors)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void refreshLocal() throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDerived()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(int updateFlags) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IResourceAttributes getResourceAttributes()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setResourceAttributes(IResourceAttributes attributes) throws IOException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URI getLocationURI()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public int compareTo(Project o)
    {
        return getName().equalsIgnoreCase(o.getName()) ? 0 : 1;
    }

    public String toString()
    {
        return super.toString() + " [" + name + "]";
    }
}
