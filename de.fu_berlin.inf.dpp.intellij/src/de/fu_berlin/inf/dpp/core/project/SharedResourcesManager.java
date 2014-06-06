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

package de.fu_berlin.inf.dpp.core.project;

import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.exceptions.CoreException;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.core.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.*;
import de.fu_berlin.inf.dpp.intellij.concurrent.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.project.events.FileSystemChangeListener;
import de.fu_berlin.inf.dpp.intellij.project.events.IResourceListener;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducerAndConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;
import org.picocontainer.annotations.Inject;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This manager is responsible for handling all resource changes that aren't
 * handled by the EditorManagerEcl, that is for changes that aren't done by
 * entering text in a text editor. It creates and executes file, folder, and VCS
 * activities.<br>
 * TODO Extract AbstractActivityProvider functionality in another class
 * ResourceActivityProvider, rename to SharedResourceChangeListener.
 */
/*
 * For a good introduction to Eclipse's resource change notification mechanisms
 * see
 * http://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
 */
@Component(module = "core")
public class SharedResourcesManager extends AbstractActivityProducerAndConsumer implements
        IResourceListener, Startable {
    /**
     * The {@link de.fu_berlin.inf.dpp.core.project.events.ResourceChangeEvent}s we're going to register for.
     */
    /*
     * haferburg: We're really only interested in
     * ResourceChangeEvent.POST_CHANGE events. I don't know why other events
     * were tracked, so I removed them.
     *
     * We're definitely not interested in PRE_REFRESH, refreshes are only
     * interesting when they result in an actual change, in which case we will
     * receive a POST_CHANGE event anyways.
     *
     * We also don't need PRE_CLOSE, since we'll also get a POST_CHANGE and
     * still have to test project.isOpen().
     *
     * We might want to add PRE_DELETE if the user deletes our shared project
     * though.
     */
    static final int INTERESTING_EVENTS = -1;//ResourceChangeEvent.POST_CHANGE;

    private static final Logger log = Logger
            .getLogger(SharedResourcesManager.class);

    /**
     * If the StopManager has paused the project, the SharedResourcesManager
     * doesn't react to resource changes.
     */
    protected boolean pause = false;

    protected final ISarosSession sarosSession;

    protected final StopManager stopManager;

    protected FileSystemChangeListener fileSystemListener;

    private final Map<IProject, SharedProject> sharedProjects = Collections.synchronizedMap(new HashMap<IProject, SharedProject>());
    /**
     * Should return <code>true</code> while executing resource changes to avoid
     * an infinite resource event loop.
     */
    @Inject
    protected FileReplacementInProgressObservable fileReplacementInProgressObservable;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected IWorkspace workspace;

    @Inject
    protected ConsistencyWatchdogClient consistencyWatchdogClient;

    protected Blockable stopManagerListener = new Blockable() {
        @Override
        public void unblock() {
            SharedResourcesManager.this.pause = false;
        }

        @Override
        public void block() {
            SharedResourcesManager.this.pause = true;
        }
    };

    @Override
    public void start() {
        sarosSession.addActivityProducerAndConsumer(SharedResourcesManager.this);
        stopManager.addBlockable(stopManagerListener);
        fileSystemListener.setEditorManager(editorManager);
        workspace.addResourceListener(fileSystemListener);

    }

    @Override
    public void stop() {
        workspace.removeResourceListener(fileSystemListener);
        sarosSession.addActivityProducerAndConsumer(this);
        stopManager.removeBlockable(stopManagerListener);
    }

    public SharedResourcesManager(ISarosSession sarosSession,
                                  StopManager stopManager) {
        this.sarosSession = sarosSession;
        this.stopManager = stopManager;
        this.fileSystemListener = new FileSystemChangeListener(this);
    }


    @Override
    public void exec(IActivity activity) {

        if (!(activity instanceof FileActivity
                || activity instanceof FolderActivity || activity instanceof VCSActivity)) {
            return;
        }

        try {
        /*
             * FIXME this will lockout everything. File changes made in the
             * meantime from another background job are not recognized. See
             * AddMultipleFilesTest STF test which fails randomly.
             */
            fileReplacementInProgressObservable.startReplacement();
            fileSystemListener.setEnabled(false);

            log.trace("execing " + activity.toString() + " in "
                    + Thread.currentThread().getName());

            if (activity instanceof FileActivity) {
                exec((FileActivity) activity);
            } else if (activity instanceof FolderActivity) {
                exec((FolderActivity) activity);
            } else if (activity instanceof VCSActivity) {
                exec((VCSActivity) activity);
            }

        } catch (CoreException e) {
            log.error("Failed to execute resource activity.", e);
        } finally {
            fileReplacementInProgressObservable.replacementDone();
            fileSystemListener.setEnabled(true);
            log.trace("done execing " + activity.toString());
        }
    }

    protected void exec(FileActivity activity) throws CoreException {

        if (activity.isRecovery()) {
            handleFileRecovery(activity);
            return;
        }

        // TODO check if we should open / close existing editors here too
        switch (activity.getType()) {
            case CREATED:
                handleFileCreation(activity);
                break;
            case REMOVED:
                handleFileDeletion(activity);
                break;
            case MOVED:
                handleFileMove(activity);
                break;
        }
    }

    private void handleFileRecovery(FileActivity activity) throws CoreException {
        SPath path = activity.getPath();

        log.debug("performing recovery for file: "
                + activity.getPath().getFullPath());


        FileActivity.Type type = activity.getType();

        try {
            if (type == FileActivity.Type.CREATED) {
                handleFileCreation(activity);
            } else if (type == FileActivity.Type.REMOVED) {
                editorManager.getActionManager().closeFile(path);
                handleFileDeletion(activity);
            } else {
                log.warn("performing recovery for type " + type
                        + " is not supported");
            }
        } finally {
            /*
             * always reset Jupiter or we will get into trouble because the
             * vector time is already reseted on the host
             */
            sarosSession.getConcurrentDocumentClient().reset(path);
        }

        //todo: generates error as looks like Jupiter on server side is will be reset later
        //consistencyWatchdogClient.performCheck(path);
    }

    private void handleFileMove(FileActivity activity) throws CoreException {
        IPath newFilePath = activity.getPath().getFullPath();
        IResource oldResource = activity.getOldPath().getResource();

        FileUtils.mkdirs(activity.getPath().getResource());
        FileUtils.move(newFilePath, oldResource);

        if (activity.getContents() == null)
            return;

        handleFileCreation(activity);
    }

    private void handleFileDeletion(FileActivity activity) throws CoreException {
        IFile file = activity.getPath().getFile();


        if (file.exists()) {
            fileSystemListener.setEnabled(false);
            fileSystemListener.addIncoming(file.toFile());
            FileUtils.delete(file);
            fileSystemListener.setEnabled(true);
        } else {
            log.warn("could not delete file " + file
                    + " because it does not exist");
        }
    }

    private void handleFileCreation(FileActivity activity) throws CoreException {

        //We need to try replace directly in document if it is open
        boolean replaced = false;

        String newText = new String(activity.getContents(), EncodingProjectManager.getInstance().getDefaultCharset());
        replaced = editorManager.getActionManager().replaceText(activity.getPath(), newText);

        if (replaced) {
            editorManager.getActionManager().saveFile(activity.getPath());

        } else {
            IFile file = activity.getPath().getFile();
            byte[] actualContent = FileUtils.getLocalFileContent(file);
            byte[] newContent = activity.getContents();

            if (!Arrays.equals(newContent, actualContent)) {
                fileSystemListener.setEnabled(false);
                fileSystemListener.addIncoming(file.toFile());
                FileUtils.writeFile(new ByteArrayInputStream(newContent), file,
                        new NullProgressMonitor());
                fileSystemListener.setEnabled(true);
            } else {
                log.info("FileActivity " + activity + " dropped (same content)");
            }
        }
    }

    protected void exec(FolderActivity activity) throws CoreException {

        SPath path = activity.getPath();

        IFolder folder = path.getProject().getFolder(path.getProjectRelativePath());
        fileSystemListener.setEnabled(false);
        fileSystemListener.addIncoming(folder.getFullPath().toFile());

        if (activity.getType() == FolderActivity.Type.CREATED) {
            FileUtils.create(folder);
        } else if (activity.getType() == FolderActivity.Type.REMOVED) {
            try {
                if (folder.exists()) {
                    FileUtils.delete(folder);
                }
            } catch (CoreException e) {
                log.warn("Removing folder failed: " + folder);
            }
        }

        fileSystemListener.setEnabled(true);
    }

    protected void exec(VCSActivity activity) {
        final VCSActivity.Type activityType = activity.getType();
        SPath path = activity.getPath();

        /* final IResource resource = ((EclipseResourceImpl) path.getResource())
                .getDelegate();

        final IProject project = ((EclipseProjectImpl) path.getProject())
                .getDelegate();

        final String url = activity.getURL();
        final String directory = activity.getDirectory();
        final String revision = activity.getParam1();

        // Connect is special since the project doesn't have a VCSAdapter
        // yet.
        final VCSAdapter vcs = activityType == VCSActivity.Type.CONNECT ? VCSAdapter
                .getAdapter(revision) : VCSAdapter.getAdapter(project);
        if (vcs == null) {
            log.warn("Could not execute VCS activity. Do you have the Subclipse plug-in installed?");
            if (activity.containedActivity.size() > 0) {
                log.trace("contained activities: "
                        + activity.containedActivity.toString());
            }
            for (IResourceActivity a : activity.containedActivity) {
                exec(a);
            }
            return;
        }

        try {
            // TODO Should these operations run in an IWorkspaceRunnable?
            Shell shell = SWTUtils.getShell();
            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(
                    shell);
            progressMonitorDialog.open();
            Shell pmdShell = progressMonitorDialog.getShell();
            pmdShell.setText("Saros running VCS operation");
            log.trace("about to call progressMonitorDialog.run");
            progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor progress)

                        throws InvocationTargetException, InterruptedException {
                    log.trace("progressMonitorDialog.run started");
                    if (!SWTUtils.isSWT())
                        log.trace("not in SWT thread");
                    if (activityType == VCSActivity.Type.CONNECT) {
                        vcs.connect(project, url, directory, progress);
                    } else if (activityType == VCSActivity.Type.DISCONNECT) {
                        vcs.disconnect(project, revision != null, progress);
                    } else if (activityType == VCSActivity.Type.SWITCH) {
                        vcs.switch_(resource, url, revision, progress);
                    } else if (activityType == VCSActivity.Type.UPDATE) {
                        vcs.update(resource, revision, progress);
                    } else {
                        log.error("VCS activity type not implemented yet.");
                    }
                    log.trace("progressMonitorDialog.run done");
                }

            });
            pmdShell.dispose();
        } catch (InvocationTargetException e) {
            // TODO We can't get here, right?
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            log.error("Code not designed to be interrupted!");
        }*/
    }

    // HACK
    public void projectAdded(IProject project) {

        //todo
        /* synchronized (sharedProjects) {
            IProject eclipseProject = ((EclipseProjectImpl) project)
                    .getDelegate();
            sharedProjects.put(eclipseProject, new SharedProject(
                    eclipseProject, sarosSession));
        }*/
    }

    // HACK
    public void projectRemoved(IProject project) {

        //todo
        /*synchronized (sharedProjects) {

          *//*  SharedProject sharedProject = sharedProjects
                    .removeAll(((EclipseProjectImpl) project).getDelegate());
            if (sharedProject != null)
                sharedProject.delete();
                *//*
        }*/
    }

    public ISarosSession getSession() {
        return sarosSession;
    }
}

