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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.4
 * Time: 12.01
 */

public class FolderImp extends AbstractContainer implements IFolder
{

    public FolderImp(Project project, File file)
    {
        super(project, file);
    }

    public FolderImp(File file)
    {
        super(file);
    }

    @Override
    public void create(int updateFlags, boolean local) throws IOException
    {
        System.out.println("FolderImp.create");
        //todo
    }

    @Override
    public void create(boolean force, boolean local) throws IOException
    {
        System.out.println("FolderImp.create");
        //todo
    }



    @Override
    public IResource[] members() throws IOException
    {
         return members(NONE);
    }


    @Override
    public IResource[] members(int memberFlags) throws IOException
    {
        List<IResource> list = new ArrayList<IResource>();

        for (File myFile : file.listFiles())
        {
             if(myFile.isFile() && !myFile.isHidden()
                     && (memberFlags==NONE || memberFlags==FILE))
             {
                 list.add(new FileImp(file));
             }

            if(myFile.isDirectory() && !myFile.isHidden()
                    && (memberFlags==NONE || memberFlags==FOLDER))
            {
                list.add(new FolderImp(file));
            }
        }

        return list.toArray(new IResource[]{});
    }


    @Override
    public int getType()
    {
        return FOLDER;
    }


    @Override
    public void delete(int updateFlags) throws IOException
    {
        System.out.println("FolderImp.delete");
        //todo
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException
    {
        System.out.println("FolderImp.move");
        //todo
    }


    @Override
    public Object getAdapter(Class<? extends IResource> clazz)
    {
        System.out.println("FolderImp.getAdapter");
        return null;  //todo
    }
}
