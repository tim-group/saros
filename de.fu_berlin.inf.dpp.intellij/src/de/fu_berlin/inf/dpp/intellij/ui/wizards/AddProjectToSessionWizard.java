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
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoWithProgressPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.SelectProjectPage;
import de.fu_berlin.inf.dpp.invitation.FileList;
import de.fu_berlin.inf.dpp.invitation.FileListDiff;
import de.fu_berlin.inf.dpp.invitation.FileListFactory;
import de.fu_berlin.inf.dpp.invitation.ProcessTools;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wizard for adding project to session
 */
public class AddProjectToSessionWizard {
    private static Logger LOG = Logger
        .getLogger(AddProjectToSessionWizard.class);

    public static final String INFO_PAGE_ID = "infoPage";
    public static final String FILE_LIST_PAGE_ID = "fileListPage";
    public static final String PROGRESS_PAGE_ID = "progressPage";

    private final Map<String, String> remoteProjectNames;

    protected IncomingProjectNegotiation process;
    protected JID peer;
    protected List<FileList> fileLists;

    /**
     * projectID => projectName
     */
    protected Map<String, IProject> remoteProjects;

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

    @Inject
    private Saros saros;

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

            for (Map.Entry<String, String> entry : remoteProjectNames.entrySet()) {
                final String projectID = entry.getKey();
                final String projectName = entry.getValue();
                IProject project = saros.getWorkspace().getProject(projectName);

                remoteProjects.put(projectID, project);
            }

            final boolean checkFiles = isExisting;
            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    if (checkFiles) {
                        createAndOpenProjects(remoteProjects);
                        runCalculateChangedFiles(remoteProjects);
                    } else {
                        createAndOpenProjects(remoteProjects);
                        runAddProject();
                    }
                }
            });

        }

        @Override
        public void cancel() {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    process.localCancel("Not accepted",
                        ProcessTools.CancelOption.NOTIFY_PEER);
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
            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    runAddProject();
                }
            });
        }

        @Override
        public void cancel() {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    process.localCancel("Not accepted",
                        ProcessTools.CancelOption.NOTIFY_PEER);
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
        this.remoteProjects = new HashMap<String, IProject>();

        String prjName = projectNames.values().iterator().next();

        wizard = new Wizard(Messages.AddProjectToSessionWizard_title);

        wizard.setHeadPanel(
            new HeaderPanel(Messages.EnterProjectNamePage_title2, ""));

        infoPage = new SelectProjectPage(INFO_PAGE_ID);
        infoPage.setNewProjectName(prjName); //todo
        infoPage.setProjectName(prjName);
        infoPage.setProjectBase(workspace.getLocation().toOSString());
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

    private void runCalculateChangedFiles(Map<String, IProject> projectMapping) {

        IProgressMonitor monitor = fileListPage.getProgressMonitor(true, false);
        monitor.setTaskName("Calculating changed files...");

        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        final Map<String, IProject> sources = new HashMap<String, IProject>();
        for (FileList fList : fileLists) {
            IProject localProject = projectMapping
                .get(fList.getProjectID());
            try {
                localProject.refreshLocal();
            } catch (IOException e) {
                LOG.error(e);
            }
            sources.put(fList.getProjectID(), localProject);
        }
        modifiedProjects.putAll(getModifiedProjects(sources));
        try {
            modifiedResources
                .putAll(getModifiedResources(modifiedProjects, monitor));
        } catch (IOException e) {
            LOG.error(e);
            DialogUtils.showError(wizard.getWizard(), "Calculation error",
                "Error while calculating modified resources: " + e
                    .getMessage());
            wizard.close();

        }

        int writeOverCount = 0;
        for (String key : modifiedResources.keySet()) {
            // String prjName = remoteProjects.get(key);
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
            SafeDialogUtils.showWarning(
                writeOverCount + " files local changes will be overwritten!",
                "Warning");
        }

    }

    public void runAddProject() {
        final IProgressMonitor monitor = progressPage
            .getProgressMonitor(true, true);
        process.run(remoteProjects, monitor, false);
    }

    protected Component getShell() {
        return saros.getMainPanel();
    }

    private Collection<SPath> getOpenEditorsForSharedProjects(
        Collection<IProject> projects) {

        //todo: filter by project
        return editorManager.getLocallyOpenEditors();
    }

    //todo: implementation needed
    public void cancelWizard(JID peer, String errorMsg,
        ProcessTools.CancelLocation type) {

    }

    //todo: implementation needed
    public boolean performFinish() {
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

    public void createAndOpenProjects(Map<String, IProject> projectMapping) {

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {
            IProject project = entry.getValue();
            try {
                if (!project.exists()) {
                    ((ProjectImp) project).create();
                }

                if (!project.isOpen()) {
                    project.open();
                }

            } catch (IOException e) {
                LOG.error("Could not create project", e);
                DialogUtils
                    .showError(wizard.getWizard(), "Could not create project.",
                        "Error");
            }
        }
    }

    /**
     * Returns all modified resources (either changed or deleted) for the
     * current project mapping.
     */
    private Map<String, FileListDiff> getModifiedResources(
        Map<String, IProject> projectMapping, IProgressMonitor monitor)
        throws IOException {
        Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        ISarosSession session = sessionManager.getSarosSession();

        // FIXME the wizard should handle the case that the session may stop in
        // the meantime !

        if (session == null) {
            throw new IllegalStateException("no session running");
        }

        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
            projectMapping.size() * 2);
        subMonitor
            .setTaskName("\"Searching for files that will be modified...\",");

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {

            String projectID = entry.getKey();
            IProject project = entry.getValue();

            FileListDiff diff;

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

                    List<IResource> eclipseResources = session
                        .getSharedResources(project);

                    //FIXME: Change VCSprovider back from null, if VCS support is added
                    FileList sharedFileList = FileListFactory
                        .createFileList(project, eclipseResources,
                            checksumCache, null,
                            new SubProgressMonitor(monitor, 1,
                                SubProgressMonitor.SUPPRESS_SETTASKNAME));

                    // FIXME FileList objects should be immutable after creation
                    remoteFileList.getPaths().addAll(sharedFileList.getPaths());
                } else {
                    subMonitor.worked(1);
                }

                //FIXME: Change VCSprovider back from null, if VCS support is added
                diff = FileListDiff.diff(FileListFactory
                        .createFileList(project, null, checksumCache, null,
                            new SubProgressMonitor(monitor, 1,
                                SubProgressMonitor.SUPPRESS_SETTASKNAME)),
                    remoteFileList);

                if (process.isPartialRemoteProject(projectID)) {
                    diff.clearRemovedPaths();
                }

                if (!diff.getRemovedPaths().isEmpty() || !diff.getAlteredPaths()
                    .isEmpty()) {
                    modifiedResources.put(project.getName(), diff);
                }

            } catch (IOException e) {
                LOG.warn("could not refresh project: " + project, e);
            }
        }
        return modifiedResources;
    }

}
