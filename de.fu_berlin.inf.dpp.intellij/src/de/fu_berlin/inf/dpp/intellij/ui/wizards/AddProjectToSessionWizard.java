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

package de.fu_berlin.inf.dpp.intellij.ui.wizards;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.invitation.FileList;
import de.fu_berlin.inf.dpp.core.invitation.FileListDiff;
import de.fu_berlin.inf.dpp.core.invitation.FileListFactory;
import de.fu_berlin.inf.dpp.intellij.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.core.monitor.Status;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.ui.*;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.exception.CoreException;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileUtil;
import de.fu_berlin.inf.dpp.intellij.runtime.Job;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.MessageDialog;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SarosView;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.PageActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.Wizard;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoWithProgressPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.SelectProjectPage;
import de.fu_berlin.inf.dpp.invitation.ProcessTools;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 14.08
 */

public class AddProjectToSessionWizard implements IAddProjectToSessionWizard {
    public static final String INFO_PAGE_ID = "infoPage";
    public static final String FILE_LIST_PAGE_ID = "fileListPage";
    public static final String PROGRESS_PAGE_ID = "progressPage";

    private static Logger log = Logger.getLogger(AddProjectToSessionWizard.class);

    protected IEnterProjectNamePage namePage; //todo

    protected IWizardDialogAccessible wizardDialog; //todo

    protected IncomingProjectNegotiation process;
    protected JID peer;
    protected List<FileList> fileLists;
    private ISarosView sarosView = new SarosView();

    /**
     * projectID => projectName
     */
    protected Map<String, String> remoteProjectNames;


    @Inject
    private IChecksumCache checksumCache;

    @Inject
    private DataTransferManager dataTransferManager;

    @Inject
    private PreferenceUtils preferenceUtils;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private IWorkspace workspace;

    @Inject
    private EditorManager editorManager;

    private Wizard wizard;
    private SelectProjectPage infoPage;
    private ProgressPage progressPage;
    private InfoWithProgressPage fileListPage;

    private PageActionListener infoPageListener = new PageActionListener() {
        @Override
        public void back() {

        }

        @Override
        public void next() {
            String newName = infoPage.getNewProjectName();
            boolean isExisting = false;
            if (newName == null) {
                newName = infoPage.getExistingProjectName();
                isExisting = true;
            } else {
                wizard.getWizardModel().setNextPage(progressPage);
            }

            String key = remoteProjectNames.keySet().iterator().next();
            remoteProjectNames.put(key, newName);

            final boolean checkFiles = isExisting;
            ThreadUtils.runSafeAsync(log, new Runnable() {
                @Override
                public void run() {
                    if (checkFiles) {
                        runCalculateChangedFiles();
                    } else {
                        runAddProject();
                    }
                }
            });

        }

        @Override
        public void cancel() {
            ThreadUtils.runSafeAsync(log, new Runnable() {
                @Override
                public void run() {
                    process.localCancel("Not accepted", ProcessTools.CancelOption.NOTIFY_PEER);
                }

            });
            wizard.close();
        }
    };

    private PageActionListener fileListPageListener = new PageActionListener() {
        @Override
        public void back() {

        }

        @Override
        public void next() {
            ThreadUtils.runSafeAsync(log, new Runnable() {
                @Override
                public void run() {
                    runAddProject();
                }
            });
        }

        @Override
        public void cancel() {
            ThreadUtils.runSafeAsync(log, new Runnable() {
                @Override
                public void run() {
                    process.localCancel("Not accepted", ProcessTools.CancelOption.NOTIFY_PEER);
                }

            });
            wizard.close();
        }
    };

    public AddProjectToSessionWizard(IncomingProjectNegotiation process,
                                     JID peer, List<FileList> fileLists, Map<String, String> projectNames) {

        SarosPluginContext.initComponent(this);

        this.process = process;
        this.peer = peer;
        this.fileLists = fileLists;
        this.remoteProjectNames = projectNames;

        String prjName = projectNames.values().iterator().next();

        wizard = new Wizard(Messages.AddProjectToSessionWizard_title);

        wizard.setHeadPanel(new HeaderPanel(Messages.EnterProjectNamePage_title2, ""));

        infoPage = new SelectProjectPage(INFO_PAGE_ID);
        infoPage.setNewProjectName(prjName); //todo
        infoPage.setProjectName(prjName);
        infoPage.setProjectBase(workspace.getPath().getAbsolutePath());
        infoPage.addPageListener(infoPageListener);
        infoPage.create();
        wizard.registerPage(infoPage);

        fileListPage = new InfoWithProgressPage(FILE_LIST_PAGE_ID);
        fileListPage.setTitle("Local file changes:");
        fileListPage.create();
        fileListPage.addPageListener(fileListPageListener);

        wizard.registerPage(fileListPage);

        progressPage = new ProgressPage(PROGRESS_PAGE_ID);
        wizard.registerPage(progressPage);

        wizard.create();

        process.setProjectInvitationUI(this);

    }


    private void runCalculateChangedFiles() {

        IProgressMonitor monitor = fileListPage.getProgressMonitor(true, false);
        monitor.setTaskName("Calculating changed files...");

        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        final Map<String, IProject> sources = new HashMap<String, IProject>();
        for (FileList fList : fileLists) {
            String localProjectName = remoteProjectNames.get(fList.getProjectID());
            IProject localProject = workspace.getRoot().getProject(localProjectName);
            try {
                localProject.refreshLocal();
            } catch (IOException e) {
                log.error(e);
            }
            sources.put(fList.getProjectID(), localProject);
        }
        modifiedProjects.putAll(getModifiedProjects(sources));
        try {
            modifiedResources.putAll(calculateModifiedResources(modifiedProjects, monitor));
        } catch (IOException e) {
            e.printStackTrace();      //todo
            DialogUtils.openErrorMessageDialog(wizard.getWizard(), e.getMessage(), "Error");
            wizard.close();

        }

        int writeOverCount = 0;
        for (String key : modifiedResources.keySet()) {
            // String prjName = remoteProjectNames.get(key);
            fileListPage.addLine("Project [" + key + "]:");
            FileListDiff diff = modifiedResources.get(key);
            for (String path : diff.getAlteredPaths()) {
                fileListPage.addLine("changed: " + path);
                writeOverCount++;
            }

            for (String path : diff.getRemovedPaths()) {
                fileListPage.addLine("removed: " + path);

            }

            for (String path : diff.getAddedPaths()) {
                fileListPage.addLine("added: " + path);

            }
        }

        monitor.setTaskName("File changes calculated");
        monitor.done();

        if (writeOverCount > 0) {
            SafeDialogUtils.showWarning(writeOverCount + " files local changes will be overwritten!", "Warning");
        }

    }


    public void runAddProject() {
        final IProgressMonitor monitor = progressPage.getProgressMonitor(true, true);
        process.accept(remoteProjectNames, monitor, false);
    }

    protected Component getShell() {
        return Saros.instance().getMainPanel();
    }

    public void setWizardDlg(IWizardDialogAccessible wizardDialog) {
        this.wizardDialog = wizardDialog;
    }

    private Collection<SPath> getOpenEditorsForSharedProjects(
            Collection<IProject> projects) {

        //todo: filter by project
        return editorManager.getLocallyOpenEditors();
    }


    @Override
    public void cancelWizard(JID peer, String errorMsg, ProcessTools.CancelLocation type) {
        System.out.println("AddProjectToSessionWizard.cancelWizard");
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean performFinish() {

        final Map<String, IProject> sources = new HashMap<String, IProject>();
        final Map<String, String> projectNames = new HashMap<String, String>();
        final boolean useVersionControl = false;//namePage.useVersionControl();

        for (FileList fList : this.fileLists) {
            sources.put(fList.getProjectID(),
                    namePage.getSourceProject(fList.getProjectID()));
            projectNames.put(fList.getProjectID(),
                    namePage.getTargetProjectName(fList.getProjectID()));
        }

        List<IProject> existingProjects = new ArrayList<IProject>();

        for (IProject project : sources.values()) {
            if (project != null) {
                existingProjects.add(project);
            }
        }

        final Collection<SPath> openEditors = getOpenEditorsForSharedProjects(existingProjects);


        /*
         * Ask the user whether to overwrite local resources only if resources
         * are supposed to be overwritten based on the synchronization options
         * and if there are differences between the remote and local project.
         */
        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        modifiedProjects.putAll(getModifiedProjects(sources));

        try {
            Job job = new Job("Calculate resources") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        modifiedResources.putAll(calculateModifiedResources(modifiedProjects, monitor));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new Status(IStatus.ERROR);
                    }

                    return new Status(IStatus.OK);

                }
            };
            job.schedule();

        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause instanceof CoreException) {
                MessageDialog.openError(getShell(),
                        "Error computing file list",
                        "Could not compute local file list: " + cause.getMessage());
            } else {
                MessageDialog
                        .openError(
                                getShell(),
                                "Error computing file list",
                                "Internal error while computing local file list: "
                                        + (cause == null ? e.getMessage() : cause
                                        .getMessage())
                        );
            }

            return false;
        }
//        if (!confirmOverwritingResources(modifiedResources))
//            return false;

        /*
         * close all editors to avoid any conflicts. this will be needed for
         * rsync as it needs to move files around the file system
         */
        for (SPath editorPath : openEditors) {
            editorManager.closeEditor(editorPath);
        }

        Job job = new Job("Synchronizing") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ProjectNegotiation.Status status = process.accept(
                            projectNames, monitor, useVersionControl);

                    if (status != ProjectNegotiation.Status.OK) {
                        return Status.CANCEL_STATUS;
                    }

                    sarosView
                            .showNotification(
                                    Messages.AddProjectToSessionWizard_synchronize_finished_notification_title,
                                    MessageFormat
                                            .format(
                                                    Messages.AddProjectToSessionWizard_synchronize_finished_notification_text,
                                                    StringUtils.join(projectNames.values(),
                                                            ", ")
                                            )
                            );

                } catch (Exception e) {
                    log.error(
                            "unkown error during project negotiation: "
                                    + e.getMessage(), e
                    );
                    return Status.CANCEL_STATUS;
                } finally {
                    SWTUtils.runSafeSWTAsync(log, new Runnable() {
                        @Override
                        public void run() {
                            for (SPath editorPath : openEditors) {
                                editorManager.openEditor(editorPath);
                            }
                        }
                    });
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return true;
    }

    /**
     * Returns a project mapping that contains all projects that will be
     * modified on synchronization.
     *
     * @SWT must be called in the SWT thread context
     */
    private Map<String, IProject> getModifiedProjects(
            Map<String, IProject> projectMapping) {
        Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {
            //todo
//            if (!namePage.overwriteResources(entry.getKey()))
//            {
//                continue;
//            }

            modifiedProjects.put(entry.getKey(), entry.getValue());
        }

        return modifiedProjects;
    }

    /**
     * Returns all modified resources (either changed or deleted) for the
     * current project mapping.
     */
    private Map<String, FileListDiff> calculateModifiedResources(
            Map<String, IProject> projectMapping, IProgressMonitor monitor)
            throws IOException {
        Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        ISarosSession session = sessionManager.getSarosSession();

        // FIXME the wizard should handle the case that the session may stop in
        // the meantime !

        if (session == null) {
            throw new IllegalStateException("no session running");
        }

        ISubMonitor subMonitor = monitor.convert(
                "Searching for files that will be modified...",
                projectMapping.size() * 2);

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {

            String projectID = entry.getKey();
            IProject project = entry.getValue();
            FileUtil.create(project);

            FileListDiff diff;

            try {
                if (!project.isOpen()) {
                    project.open();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileList remoteFileList = process.getRemoteFileList(projectID);



            /*
             * do not refresh already partially shared projects as this may
             * trigger resource change events
             */
            try {
                if (!session.isShared(project)) {
                    project.refreshLocal();
                }


                if (session.isShared(project)) {

                    List<IResource> eclipseResources = session.getSharedResources(project);

                    FileList sharedFileList = FileListFactory.createFileList(
                            project, eclipseResources, checksumCache, true,
                            subMonitor.newChild(1, ISubMonitor.SUPPRESS_ALL_LABELS));

                    // FIXME FileList objects should be immutable after creation
                    remoteFileList.getPaths().addAll(sharedFileList.getPaths());
                } else {
                    subMonitor.worked(1);
                }

                diff = FileListDiff.diff(FileListFactory.createFileList(
                                project, null, checksumCache, true,
                                subMonitor.newChild(1, ISubMonitor.SUPPRESS_ALL_LABELS)),
                        remoteFileList
                );


                if (process.isPartialRemoteProject(projectID)) {
                    diff.clearRemovedPaths();
                }

                if (!diff.getRemovedPaths().isEmpty()
                        || !diff.getAlteredPaths().isEmpty()) {
                    modifiedResources.put(project.getName(), diff);
                }

            } catch (IOException e) {
                log.warn("could not refresh project: " + project, e);
            }
        }
        return modifiedResources;
    }


}
