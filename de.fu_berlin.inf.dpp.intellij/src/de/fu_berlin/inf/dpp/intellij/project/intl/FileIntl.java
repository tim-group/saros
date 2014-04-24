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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;

import java.io.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.4
 * Time: 12.02
 */

public class FileIntl extends ResourceIntl implements IFile
{
    private PsiFile psiFile;
    private PsiManager psiManager;

    public FileIntl(ProjectIntl project, VirtualFile file)
    {
        super(project, file);

        this.psiManager =  PsiManager.getInstance(project.getAdapter());
        this.psiFile =  psiManager.findFile(file);
    }

    public FileIntl(ProjectIntl project, PsiFile psiFile)
    {
        super(project, psiFile.getVirtualFile());

        this.psiManager =  PsiManager.getInstance(project.getAdapter());
        this.psiFile = psiFile;
    }

    public FileIntl(ProjectIntl project, File file)
    {
        super(project, file);

        this.psiManager =  PsiManager.getInstance(project.getAdapter());
        this.psiFile =  psiManager.findFile(getVirtualFile());
    }

    public FileIntl(ProjectIntl project, String path)
    {
        super(project, path);

        this.psiManager =  PsiManager.getInstance(project.getAdapter());
        this.psiFile =  psiManager.findFile(getVirtualFile());
    }

    @Override
    public String getCharset() throws IOException
    {
        return getDefaultCharset();
    }


    @Override
    public InputStream getContents() throws IOException
    {
        return new FileInputStream(file);
    }

    @Override
    public void setContents(InputStream input, boolean force, boolean keepHistory) throws IOException
    {
        //todo: implement force, history
        System.out.println("FileIntl.setContents //todo: force, history");

        FileOutputStream fos = new FileOutputStream(file);
        int read = -1;
        byte[] buffer = new byte[1024];
        while ((read = input.read(buffer)) != -1)
        {
            fos.write(buffer, 0, read);
        }


        fos.flush();
        fos.close();


    }

    @Override
    public void create(InputStream input, boolean force) throws IOException
    {
        setContents(input, true, true);
    }

    @Override
    public IPath getLocation()
    {
        return new PathIntl(file);
    }

    @Override
    public File toFile()
    {
        return file;
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
    public void refreshLocal() throws IOException
    {
        System.out.println("FileIntl.refreshLocal //todo");
    }


    public PsiFile getPsiFile()
    {
        return psiFile;
    }

    public PsiManager getPsiManager()
    {
        return psiManager;
    }

    @Override
    public <T> T getAdapter()
    {
        return null;
    }

    public String toString()
    {
        return file.getPath();
    }
}
