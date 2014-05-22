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

public abstract class ResourceImp implements IResource
{
    public static final String DEFAULT_CHARSET = "utf8";

    protected ProjectImp project;
    protected File file;
    private String defaultCharset = DEFAULT_CHARSET;

    private IResourceAttributes attributes;
    private boolean isDerived = false;

    protected ResourceImp(ProjectImp project, File file)
    {
        this.project = project;
        this.file = file;
        this.attributes = new FileResourceAttributes(file);
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
        return getFullPath().toFile().exists();
    }

    @Override
    public IPath getFullPath()
    {
        if (project != null && !file.isAbsolute())
        {
            return new PathImp(project.getFullPath() + File.separator + file.getPath());
        }
        else
        {
            return new PathImp(file.getAbsoluteFile());
        }
    }

    @Override
    public String getName()
    {
        return file.getName();
    }

    @Override
    public IContainer getParent()
    {
        return file == null || file.getParentFile() == null ? null : new FolderImp(project,file.getParentFile());
    }

    public ProjectImp getProject()
    {
        return project;
    }

    public void setProject(ProjectImp project)
    {
        this.project = project;
    }


    @Override
    public IPath getProjectRelativePath()
    {
        if (project == null)
        {
            return new PathImp(file);
        }
        else
        {
            File fPrj = project.getFullPath().toFile();
            if (fPrj.isFile())
            {
                fPrj = fPrj.getParentFile();
            }

            String prjPath = fPrj.getAbsolutePath();
            String myPath = file.getAbsolutePath();


            myPath = myPath.substring(prjPath.length() + 1);

            return new PathImp(new File(myPath));
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

    public File toFile()
    {
        return file;
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


}
