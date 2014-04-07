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

package de.fu_berlin.inf.dpp.intellij.project;

import de.fu_berlin.inf.dpp.filesystem.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.4
 * Time: 12.02
 */

public class FileImp extends ResourceImp implements IFile
{

    public FileImp(Project project, File file)
    {
        super(project, file);
    }

    public FileImp(File file)
    {
        super(file);
    }


    @Override
    public String getCharset() throws IOException
    {
        return getDefaultCharset();
    }


    @Override
    public InputStream getContents() throws IOException
    {
        return  new FileInputStream(file);
    }

    @Override
    public void setContents(InputStream input, boolean force, boolean keepHistory) throws IOException
    {
        //todo
    }

    @Override
    public void create(InputStream input, boolean force) throws IOException
    {
        //todo
    }

    @Override
    public IPath getLocation()
    {
        return new PathImp(file);
    }



    @Override
    public int getType()
    {
        return FILE;
    }


    @Override
    public void delete(int updateFlags) throws IOException
    {
        file.delete();
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException
    {
         file.renameTo(destination.toFile());
    }



    @Override
    public Object getAdapter(Class<? extends IResource> clazz)
    {
        System.out.println("FileImp.getAdapter");
        //todo
        return null;
    }
}
