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

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.AbstractStoppableListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.StoppableDocumentListener;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.PathImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vritual file system listener. It receives events for all files in all projects
 * opened by the user.
 * <p/>
 * It filters for files that are shared and calls the corresponding methods for
 * {@link IActivity}-creation on the {@link SharedResourcesManager}.
 */
public class FileSystemChangeListener extends AbstractStoppableListener
    implements VirtualFileListener {

    private static final Logger LOG = Logger
        .getLogger(FileSystemChangeListener.class);
    private final SharedResourcesManager resourceManager;
    private Workspace workspace;

    //HACK: This list is used to filter events for files that were created from
    //remote, because we can not disable the listener for them
    private final List<File> incomingFilesToFilterFor = new ArrayList<File>();

    private final Set<VirtualFile> newFiles = new HashSet<VirtualFile>();

    public FileSystemChangeListener(SharedResourcesManager resourceManager,
        EditorManager editorManager) {
        super(editorManager);
        this.resourceManager = resourceManager;
    }

    private void generateFolderMove(SPath oldSPath, SPath newSPath,
        boolean before) {
        User user = resourceManager.getSession().getLocalUser();
        ProjectImp project = (ProjectImp) oldSPath.getProject();
        IActivity createActivity = new FolderActivity(user,
            FolderActivity.Type.CREATED, newSPath);
        resourceManager.internalFireActivity(createActivity);

        IFolder folder = before ? oldSPath.getFolder() : newSPath.getFolder();

        IResource[] members = new IResource[0];
        try {
            members = folder.members();
        } catch (IOException e) {
            LOG.error("error reading folder: " + folder, e);
        }

        for (IResource resource : members) {
            SPath oldChildSPath = new FileImp(
                (ProjectImp) oldSPath.getProject(), new File(
                oldSPath.getFullPath().toOSString() + File.separator + resource
                    .getName()
            )
            ).getSPath();
            SPath newChildSPath = new FileImp(
                (ProjectImp) newSPath.getProject(), new File(
                newSPath.getFullPath().toOSString() + File.separator + resource
                    .getName()
            )
            ).getSPath();
            if (resource.getType() == IResource.FOLDER) {
                generateFolderMove(oldChildSPath, newChildSPath, before);
            } else {
                generateFileMove(oldChildSPath, newChildSPath, before);
            }
        }

        IActivity removeActivity = new FolderActivity(user,
            FolderActivity.Type.REMOVED, oldSPath);
        resourceManager.internalFireActivity(removeActivity);

        project.addFile(newSPath.getFile().getLocation().toFile());
        project.removeFile(oldSPath.getFile().getLocation().toFile());
    }

    private void generateFileMove(SPath oldSPath, SPath newSPath,
        boolean before) {
        User user = resourceManager.getSession().getLocalUser();
        ProjectImp project = (ProjectImp) newSPath.getProject();
        ProjectImp oldProject = (ProjectImp) oldSPath.getProject();

        IFile file;

        if (before) {
            file = oldProject.getFile(oldSPath.getFullPath());
            editorManager.saveFile(oldSPath);
        } else {
            file = project.getFile(newSPath.getFullPath());
            editorManager.saveFile(newSPath);
        }

        if (file == null) {
            return;
        }

        byte[] bytes = FileUtils.getLocalFileContent(file);

        String charset = getEncoding(file);

        IActivity activity = new FileActivity(user, FileActivity.Type.MOVED,
            newSPath, oldSPath, bytes, charset, FileActivity.Purpose.ACTIVITY);
        editorManager.replaceAllEditorsForPath(oldSPath, newSPath);

        project.addFile(newSPath.getFile().getLocation().toFile());
        oldProject.removeFile(oldSPath.getFile().getLocation().toFile());
        resourceManager.internalFireActivity(activity);
    }

    /**
     * Calls {@link EditorManager#sendTemplateContent(SPath, String)} for files
     * that were created with initial content. For other content changes itm
     * {@link StoppableDocumentListener} is used.
     * <p/>
     * This gets called for all files in the application, after they were changed.
     * This includes meta-files like workspace.xml.
     *
     * @param virtualFileEvent
     */
    @Override
    public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {
        VirtualFile virtualFile = virtualFileEvent.getFile();
        ProjectImp project = workspace.getProjectForPath(virtualFile.getPath());
        IFile file = new FileImp(project, new File(virtualFile.getPath()));

        if (resourceManager.getSession().isShared(file) && newFiles
            .contains(virtualFile)) {

            newFiles.remove(virtualFile);

            SPath spath = new SPath(project, file.getProjectRelativePath());

            //Files created from templates have initial content and are opened in
            // an editor, but do not have a DocumentListener. Their initial content
            // is transferred here, because the DocumentListener is added after
            // it was inserted
            if (editorManager.isOpenedInEditor(spath)) {
                String initialContent = null;
                try {
                    byte[] content = new byte[0];
                    content = virtualFile.contentsToByteArray();
                    initialContent = new String(content, getEncoding(file));
                } catch (IOException e) {
                    LOG.error("Could not access newly created file: " + file,
                        e);
                }

                if (initialContent != null && !initialContent.isEmpty()) {
                    editorManager.sendTemplateContent(spath, initialContent);
                }
            }
        }
    }

    /**
     * This is called after a file was created on disk, but before optional content
     * (e.g. templates) are inserted.
     *
     * @param virtualFileEvent
     */
    @Override
    public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!enabled) {
            return;
        }

        File file = convertVirtualFileEventToFile(virtualFileEvent);
        IPath path = new PathImp(file);
        ProjectImp project = workspace.getProjectForPath(file.getPath());

        if (project == null || !project.exists()) {
            return;
        }

        //This is true, when a new folder for an incoming project was created.
        //If this is the case, we do not want to send an FolderActivity back.
        if (path.equals(project.getFullPath())) {
            return;
        }

        if (incomingFilesToFilterFor.contains(file)) {
            incomingFilesToFilterFor.remove(file);
            ((ProjectImp) project).addFile(file);
            return;
        }

        if (path.equals(project.getFullPath()))
            if (!resourceManager.getSession().isCompletelyShared(project)) {
                return;
            }

        path = makeAbsolutePathProjectRelative(path, project);

        SPath spath = new SPath(project, path);
        User user = resourceManager.getSession().getLocalUser();
        IActivity activity;

        if (file.isFile()) {
            byte[] bytes = new byte[0];
            String charset;
            charset = virtualFileEvent.getFile().getCharset().name();
            activity = FileActivity.created(user, spath, bytes, charset,
                FileActivity.Purpose.ACTIVITY);

            //If the file was created with a template, it is filled only later
            //so we check for newly created files' content in {@link #contentsChanged},
            newFiles.add(virtualFileEvent.getFile());
        } else {
            activity = new FolderActivity(user, FolderActivity.Type.CREATED,
                spath);
        }

        ((ProjectImp) project).addFile(file);

        resourceManager.internalFireActivity(activity);
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!enabled) {
            return;
        }

        File file = convertVirtualFileEventToFile(virtualFileEvent);
        if (incomingFilesToFilterFor.contains(file)) {
            incomingFilesToFilterFor.remove(file);
            return;
        }

        IPath path = new PathImp(file);
        ProjectImp project = workspace.getProjectForPath(file.getPath());

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        path = makeAbsolutePathProjectRelative(path, project);

        SPath spath = new SPath(project, path);
        User user = resourceManager.getSession().getLocalUser();

        IActivity activity;
        if (virtualFileEvent.getFile().isDirectory()) {
            activity = new FolderActivity(user, FolderActivity.Type.REMOVED,
                spath);
        } else {
            activity = FileActivity
                .removed(user, spath, FileActivity.Purpose.ACTIVITY);
        }

        ((ProjectImp) project).removeFile(file);
        editorManager.removeAllEditorsForPath(spath);

        resourceManager.internalFireActivity(activity);
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
        if (!enabled) {
            return;
        }

        File newFile = convertVirtualFileEventToFile(virtualFileMoveEvent);
        if (incomingFilesToFilterFor.contains(newFile)) {
            incomingFilesToFilterFor.remove(newFile);
            return;
        }

        IPath path = new PathImp(newFile);
        ProjectImp project = workspace.getProjectForPath(newFile.getPath());

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        path = makeAbsolutePathProjectRelative(path, project);

        SPath newSPath = new SPath(project, path);

        IPath oldPath = new PathImp(new File(
            virtualFileMoveEvent.getOldParent() + File.separator
                + virtualFileMoveEvent.getFileName()
        ));
        IProject oldProject = workspace.getProjectForPath(oldPath.toPortableString());

        oldPath = makeAbsolutePathProjectRelative(oldPath, project);
        SPath oldSPath = new SPath(oldProject, oldPath);

        //FIXME: Handle cases where files are moved from outside the shared project
        //into the shared project
        if (oldProject == null) {
            LOG.error(" can not move files from unshared project to shared project");
            return;
        }
        if (project.equals(oldProject)) {
            if (newFile.isFile()) {
                generateFileMove(oldSPath, newSPath, false);
            } else {
                generateFolderMove(oldSPath, newSPath, false);
            }
        }
    }

    @Override
    public void propertyChanged(
        @NotNull VirtualFilePropertyEvent filePropertyEvent) {
        if (!enabled) {
            return;
        }

        File oldFile = new File(
            filePropertyEvent.getFile().getParent().getPath() + File.separator
                + filePropertyEvent.getOldValue()
        );
        File newFile = convertVirtualFileEventToFile(filePropertyEvent);

        if (incomingFilesToFilterFor.contains(newFile)) {
            incomingFilesToFilterFor.remove(newFile);
            return;
        }

        IPath oldPath = new PathImp(oldFile);
        ProjectImp project = workspace.getProjectForPath(newFile.getPath());

        if (project == null || !project.exists()) {
            return;
        }

        oldPath = makeAbsolutePathProjectRelative(oldPath, project);
        SPath oldSPath = new SPath(project, oldPath);

        IPath newPath = new PathImp(newFile);
        newPath = makeAbsolutePathProjectRelative(newPath, project);

        SPath newSPath = new SPath(project, newPath);
        //we handle this as a move activity
        if (newFile.isFile()) {
            generateFileMove(oldSPath, newSPath, false);
        } else {
            generateFolderMove(oldSPath, newSPath, false);
        }
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {
        if (!enabled) {
            return;
        }

        VirtualFile virtualFile = virtualFileCopyEvent.getFile();
        File newFile = new File(virtualFile.getPath());

        if (incomingFilesToFilterFor.contains(newFile)) {
            incomingFilesToFilterFor.remove(newFile);
            return;
        }

        IPath path = new PathImp(newFile);
        ProjectImp project = workspace.getProjectForPath(newFile.getPath());

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        path = makeAbsolutePathProjectRelative(path, project);

        SPath spath = new SPath(project, path);

        User user = resourceManager.getSession().getLocalUser();
        IActivity activity;

        byte[] bytes = new byte[0];
        try {
            bytes = virtualFileCopyEvent.getOriginalFile()
                .contentsToByteArray();
        } catch (IOException e) {
            Workspace.LOG.error("could not read content of original file " + virtualFileCopyEvent.getOriginalFile(), e);
            return;
        }

        activity = FileActivity
            .created(user, spath, bytes, virtualFile.getCharset().name(),
                FileActivity.Purpose.ACTIVITY);

        ((ProjectImp) project).addFile(newFile);

        resourceManager.internalFireActivity(activity);
    }

    @Override
    public void beforePropertyChange(
        @NotNull VirtualFilePropertyEvent filePropertyEvent) {

    }

    /**
     * This method is called for files that already exist and that are modified,
     * but before the file is modified on disk.
     *
     * @param virtualFileEvent
     */
    @Override
    public void beforeContentsChange(
        @NotNull VirtualFileEvent virtualFileEvent) {
        //Do nothing
    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {
        //Do nothing
    }

    @Override
    public void beforeFileMovement(
        @NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
        //Do nothing
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Adds a file to the filter list for incoming files (for which no activities
     * should be generated).
     *
     * @param file
     */
    public void addIncomingFileToFilterFor(File file) {
        incomingFilesToFilterFor.add(file);
    }

    private IPath makeAbsolutePathProjectRelative(IPath path,
        IProject project) {
        int projSegmentCount = project.getFullPath().segments().length;
        path = path.removeFirstSegments(projSegmentCount);
        return path;
    }

    private File convertVirtualFileEventToFile(
        VirtualFileEvent virtualFileEvent) {
        return new File(virtualFileEvent.getFile().getPath());
    }

    private String getEncoding(IFile file) {
        String charset = null;

        try {
            charset = file.getCharset();
        } catch (IOException e) {
            LOG.warn("could not determine encoding for file: " + file, e);
        }
        if (charset == null)
            return EncodingProjectManager.getInstance().getDefaultCharset()
                .name();

        return charset;
    }
}
