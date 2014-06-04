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

package de.fu_berlin.inf.dpp.intellij.project.events;

import com.intellij.openapi.vfs.*;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.events.AbstractStoppableListener;
import de.fu_berlin.inf.dpp.intellij.project.fs.PathImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.session.User;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-19
 * Time: 16:45
 */

public class FileSystemChangeListener extends AbstractStoppableListener implements VirtualFileListener {

    private IResourceListener resourceManager;
    private Workspace workspace;
    private List<File> incomingList = new ArrayList<File>();
    private EditorManager editorManager;

    public FileSystemChangeListener(IResourceListener resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!enabled) {
            return;
        }

        File file = new File(virtualFileEvent.getFile().getPath());
        IPath path = new PathImp(file);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (incomingList.contains(file)) {
            incomingList.remove(file);

            ((ProjectImp) project).addFile(file);

            return;
        }


        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getLocation().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath spath = new SPath(project, path);
        User user = resourceManager.getSession().getLocalUser();
        IActivity activity;
        if (file.isFile()) {
            byte[] bytes = new byte[0];
            try {
                bytes = virtualFileEvent.getFile().contentsToByteArray();
            } catch (IOException e) {
                workspace.log.error(e.getMessage(), e);
            }

            activity = FileActivity.created(user, spath, bytes, FileActivity.Purpose.ACTIVITY);

        } else {
            activity = new FolderActivity(user, FolderActivity.Type.CREATED, spath);

        }

        ((ProjectImp) project).addFile(file);

        resourceManager.fireActivity(activity);
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!enabled) {
            return;
        }

        File file = new File(virtualFileEvent.getFile().getPath());
        if (incomingList.contains(file)) {
            incomingList.remove(file);
            return;
        }


        IPath path = new PathImp(file);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getLocation().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath spath = new SPath(project, path);
        User user = resourceManager.getSession().getLocalUser();

        IActivity activity;
        if (file.isFile()) {
            activity = FileActivity.removed(user, spath, FileActivity.Purpose.ACTIVITY);
        } else {
            activity = new FolderActivity(user, FolderActivity.Type.REMOVED, spath);
        }

        ((ProjectImp) project).removeFile(file);

        resourceManager.fireActivity(activity);
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
        if (!enabled) {
            return;
        }

        File newFile = new File(virtualFileMoveEvent.getFile().getPath());
        if (incomingList.contains(newFile)) {
            incomingList.remove(newFile);
            return;
        }

        IPath path = new PathImp(newFile);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getLocation().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath spath = new SPath(project, path);

        IPath oldPath = new PathImp(new File(virtualFileMoveEvent.getOldParent() + File.separator + virtualFileMoveEvent.getFileName()));
        oldPath = oldPath.removeFirstSegments(projSegmentCount);

        SPath sOldPath = new SPath(project, oldPath);

        User user = resourceManager.getSession().getLocalUser();
        IActivity activity;

        byte[] bytes = new byte[0];
        try {
            bytes = virtualFileMoveEvent.getFile().contentsToByteArray();
        } catch (IOException e) {
            workspace.log.error(e.getMessage(), e);
        }

        activity = new FileActivity(user, FileActivity.Type.MOVED, spath, sOldPath, bytes, FileActivity.Purpose.ACTIVITY);

        ((ProjectImp) project).removeFile(oldPath.toFile());
        ((ProjectImp) project).addFile(newFile);

        resourceManager.fireActivity(activity);
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {
        if (!enabled) {
            return;
        }

        File newFile = new File(virtualFileCopyEvent.getFile().getPath());
        if (incomingList.contains(newFile)) {
            incomingList.remove(newFile);
            return;
        }

        IPath path = new PathImp(newFile);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getLocation().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath spath = new SPath(project, path);


        User user = resourceManager.getSession().getLocalUser();
        IActivity activity;

        byte[] bytes = new byte[0];
        try {
            bytes = virtualFileCopyEvent.getOriginalFile().contentsToByteArray();
        } catch (IOException e) {
            workspace.log.error(e.getMessage(), e);
        }

        activity = FileActivity.created(user, spath, bytes, FileActivity.Purpose.ACTIVITY);

        ((ProjectImp) project).addFile(newFile);

        resourceManager.fireActivity(activity);
    }

    @Override
    public void beforePropertyChange(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void addIncoming(File file) {
        incomingList.add(file);
    }

    public void setEditorManager(EditorManager editorManager) {
        this.editorManager = editorManager;
    }
}
