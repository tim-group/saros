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

package de.fu_berlin.inf.dpp.intellij.editor;

/**
 * Created by holger on 16.07.14.
 */
public abstract class AbstractEditorActionHandler {
/*
    private VirtualFile toVirtualFile(SPath path) {
        return toVirtualFile(path.getFile().getLocation().toFile());
    }

    protected VirtualFile toVirtualFile(File path) {
        return localFileSystem.refreshAndFindFileByIoFile(path);
    }


    protected SPath toPath(VirtualFile virtualFile) {
        if (virtualFile == null || !virtualFile.exists() || !manager.hasSession()) {
            return null;
        }

        IResource resource = null;
        String path = virtualFile.getPath();

        for (IProject project : manager.getSession().getProjects()) {
            resource = project.getFile(path);
            if (resource != null) {
                break;
            }

        }
        return resource == null ? null : new SPath(resource);
    }*/
}
