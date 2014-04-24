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

import de.fu_berlin.inf.dpp.filesystem.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 15.37
 */

public class ProjectImp implements IProject
{
    private String name;
    private File path;

    private Map<IPath, IResource> resourceMap = new HashMap<IPath, IResource>();
    private Map<String, IFile> fileMap = new HashMap<String, IFile>();
    private Map<String, IFolder> folderMap = new HashMap<String, IFolder>();

    private boolean isOpen;
    private String defaultCharset = DEFAULT_CHARSET;
    private boolean exist;
    private IPath fullPath;
    private IPath relativePath;
    private IContainer parent;
    private boolean isAccessible;
    private IResourceAttributes attributes;


    public ProjectImp(String name)
    {
        this.name = name;
    }

    public ProjectImp(String name, File path)
    {
        this.name = name;
        this.path = path;
        scan(path);
    }

    public void setPath(File path)
    {
        this.path = path;

        exist = false;
        isAccessible = false;
        fullPath = new PathImp(path.getAbsolutePath());
        relativePath = new PathImp(path.getPath());

        attributes = new ResourceAttributes(); //todo
    }

    public void scan(File path)
    {
        this.path = path;
        //clear old
        resourceMap.clear();
        fileMap.clear();
        folderMap.clear();
        if (!path.exists())
        {
            path.mkdirs();
        }
        else
        {
            addRecursive(path);
        }

        exist = true;
        isAccessible = true;
        fullPath = new PathImp(path.getAbsolutePath());
        relativePath = new PathImp(path.getPath());

        attributes = new ResourceAttributes(); //todo
    }

    protected void addRecursive(File file)
    {


        if (file.isDirectory())
        {
            for (File myFile : file.listFiles())
            {
                addRecursive(myFile);
            }
        }
        else
        {
            addFile(file);
        }
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public IResource findMember(IPath path)
    {
        return resourceMap.get(path);
    }


    @Override
    public void accept(IResourceVisitor visitor, int resource, int container)
    {
        //todo: Implement it
        System.out.println("ProjectIntl.accept //todo");
    }

    /**
     * @param res
     */
    protected void addResource(IResource res)
    {
        resourceMap.put(res.getFullPath(), res);
    }

    protected void addResource(IFile file)
    {
        addResource((IResource) file);
        fileMap.put(file.getFullPath().toString(), file);
    }

    protected void addResource(IFolder folder)
    {
        addResource((IResource) folder);
        folderMap.put(folder.getFullPath().toString(), folder);
    }

    public void addFile(File file)
    {
        if (file.isDirectory())
        {
            IFolder folder = new FolderImp(this, file);
            addResource(folder);
        }

        if (file.isFile())
        {
            IFile myFile = new FileImp(this, file);
            addResource(myFile);
        }
    }

    @Override
    public IFile getFile(String name)
    {
        return fileMap.get(name);
    }

    @Override
    public IFile getFile(IPath path)
    {
        IFile f = getFile(path.toPortableString());

        if (f == null)
        {
            f = new FileImp(this, new File(this.path.getPath() + "/" + path.toPortableString()));
        }

        System.out.println("ProjectImpl.getFile " + path + " -> " + f+" exist="+f.exists());
        //System.out.println(fileMap);

        return f;
    }

    @Override
    public IFolder getFolder(String name)
    {
        return folderMap.get(name);
    }

    @Override
    public IFolder getFolder(IPath path)
    {
        return getFolder(path.toPortableString());
    }

    @Override
    public boolean isOpen()
    {
        return isOpen;
    }

    @Override
    public void open() throws IOException
    {
        this.isOpen = true;
    }

    @Override
    public boolean exists(IPath path)
    {
        return resourceMap.containsKey(path);
    }

    @Override
    public IResource[] members()
    {
        return resourceMap.values().toArray(new IResource[]{});
    }

    @Override
    public IResource[] members(int memberFlags)
    {
        List<IResource> list = new ArrayList<IResource>();
        for (IResource res : resourceMap.values())
        {
            if (memberFlags == FOLDER && res.getType() == FOLDER)
            {
                list.add(res);
            }
            else if (memberFlags == FILE && res.getType() == FILE)
            {
                list.add(res);
            }
            else if (memberFlags == NONE)
            {
                list.add(res);
            }
        }

        return list.toArray(new IResource[]{});

    }

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
        return exist;
    }

    public void setExist(boolean exist)
    {
        this.exist = exist;
    }

    @Override
    public IPath getFullPath()
    {
        return fullPath;
    }

    public void setFullPath(IPath fullPath)
    {
        this.fullPath = fullPath;
    }

    public void setRelativePath(IPath relativePath)
    {
        this.relativePath = relativePath;
    }

    @Override
    public String getName()
    {
        return name;
    }

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
    public IProject getProject()
    {
        return this;
    }

    @Override
    public IPath getProjectRelativePath()
    {
        return this.relativePath;
    }

    @Override
    public int getType()
    {
        return IResource.PROJECT;
    }

    @Override
    public boolean isAccessible()
    {
        return isAccessible;
    }

    public void setAccessible(boolean accessible)
    {
        isAccessible = accessible;
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
    public void refreshLocal() throws IOException
    {
        scan(path);
    }


    @Override
    public void delete(int updateFlags) throws IOException
    {
        FileUtils.deleteDirectory(path);
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException
    {
       path.renameTo(destination.toFile());
    }

    @Override
    public IResourceAttributes getResourceAttributes()
    {
        return attributes;
    }

    @Override
    public void setResourceAttributes(IResourceAttributes attributes) throws IOException
    {
        this.attributes = attributes;
    }

    @Override
    public URI getLocationURI()
    {
        try
        {
            return new URI(fullPath.toString());
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public <T> T getAdapter()
    {
        return null;
    }

    @Override
    public IPath getLocation()
    {
        return this.fullPath;
    }

    @Override
    public File toFile()
    {
        return path;
    }

    public String toString()
    {
        return super.toString() + " [" + name + "]";
    }


    @Override
    public int hashCode()
    {
        int hash = 1;
        hash = hash * 17 + this.name.hashCode();
        hash = hash * 31 + this.path.hashCode();
        return hash;

    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof ProjectImp))
            return false;

        ProjectImp other = (ProjectImp)obj;

        return this.name.equals(other.name) && this.path.equals(other.path);
    }
}
