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

public class FolderImp extends ResourceImp implements IFolder {

    public FolderImp(ProjectImp project, File file) {
        super(project, file);
    }


    @Override
    public void create(int updateFlags, boolean local) throws IOException {
        if (!file.isAbsolute()) {
            File fileInProject = new File(getProject().getFullPath().toString() + File.separator + file.getPath());
            fileInProject.mkdirs();
        } else {
            file.mkdirs();
        }
    }

    @Override
    public void create(boolean force, boolean local) throws IOException {
        file.mkdirs();
    }

    @Override
    public boolean exists(IPath path) {
        return new File(path.toString()).exists();
    }


    @Override
    public IResource[] members() {
        return members(NONE);
    }


    @Override
    public IResource[] members(int memberFlags) {
        List<IResource> list = new ArrayList<IResource>();

        for (File myFile : getFullPath().toFile().listFiles()) {
            if (myFile.isFile() && !myFile.isHidden()
                    && (memberFlags == NONE || memberFlags == FILE)) {
                list.add(new FileImp(project, myFile));
            }

            if (myFile.isDirectory() && !myFile.isHidden()
                    && (memberFlags == NONE || memberFlags == FOLDER)) {
                list.add(new FolderImp(project, myFile));
            }
        }

        return list.toArray(new IResource[]{});
    }

    @Override
    public void refreshLocal() throws IOException {
        System.out.println("FolderIntl.refreshLocal //todo");
    }

    @Override
    public int getType() {
        return FOLDER;
    }


    @Override
    public void delete(int updateFlags) throws IOException {
        if (file.isAbsolute()) {
            FileUtils.deleteDirectory(file);
        } else {
            FileUtils.deleteDirectory(getFullPath().toFile());
        }
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException {
        file.renameTo(destination.toFile());
    }


    @Override
    public Object getAdapter(Class<? extends IResource> clazz) {
        return this;
    }

    @Override
    public IPath getLocation() {
        return this.getFullPath();
    }
}
