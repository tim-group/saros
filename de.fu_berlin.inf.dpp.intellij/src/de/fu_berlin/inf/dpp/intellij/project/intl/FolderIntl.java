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

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.commons.io.FileUtils;

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

public class FolderIntl extends ResourceIntl implements IFolder
{
    private PsiDirectory psiDirectory;
    private PsiManager psiManager;

    public FolderIntl(ProjectIntl project, VirtualFile file)
    {
        super(project, file);


        this.psiDirectory = psiManager.findDirectory(getVirtualFile());
    }

    public FolderIntl(ProjectIntl project, PsiDirectory dir)
    {
        super(project, dir.getVirtualFile());
        this.psiDirectory = dir;
    }

    public FolderIntl(ProjectIntl project, File file)
    {
        super(project, file);

        this.psiDirectory = psiManager.findDirectory(getVirtualFile());
    }

    public FolderIntl(ProjectIntl project, String path)
    {
        super(project, path);
        this.psiDirectory = psiManager.findDirectory(getVirtualFile());
    }

    @Override
    public void create(int updateFlags, boolean local) throws IOException
    {
        file.mkdirs();
    }

    @Override
    public void create(boolean force, boolean local) throws IOException
    {
        file.mkdirs();
    }

    @Override
    public boolean exists(IPath path)
    {
        return new File(path.toString()).exists();
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

        for (File myFile : file.listFiles())
        {
            if (myFile.isFile() && !myFile.isHidden()
                    && (memberFlags == NONE || memberFlags == FILE))
            {
                list.add(new FileIntl(project,file));
            }

            if (myFile.isDirectory() && !myFile.isHidden()
                    && (memberFlags == NONE || memberFlags == FOLDER))
            {
                list.add(new FolderIntl(project,file));
            }
        }

        return list.toArray(new IResource[]{});
    }

    @Override
    public void refreshLocal() throws IOException
    {
        System.out.println("FolderIntl.refreshLocal //todo");
    }

    @Override
    public int getType()
    {
        return FOLDER;
    }


    @Override
    public void delete(int updateFlags) throws IOException
    {
        FileUtils.deleteDirectory(file);
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException
    {
        file.renameTo(destination.toFile());
    }

    public PsiDirectory getPsiDirectory()
    {
        return psiDirectory;
    }

    public PsiManager getPsiManager()
    {
        return psiManager;
    }

    @Override
    public Object getAdapter(Class<? extends IResource> clazz)
    {
        return this;
    }

    @Override
    public IPath getLocation()
    {
        return this.getFullPath();
    }

    @Override
    public File toFile()
    {
        return file;
    }
}
