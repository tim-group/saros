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
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.PageActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPageWithInfo;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.SelectProjectPage;
import de.fu_berlin.inf.dpp.invitation.FileList;
import de.fu_berlin.inf.dpp.invitation.FileListDiff;
import de.fu_berlin.inf.dpp.invitation.FileListFactory;
import de.fu_berlin.inf.dpp.invitation.ProcessTools;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wizard for adding projects to a session.
 */
public class AddProjectToSessionWizard {
    private static final Logger LOG = Logger
        .getLogger(AddProjectToSessionWizard.class);

    public static final String SELECT_PROJECT_PAGE_ID = "selectProject";
    public static final String FILE_LIST_PAGE_ID = "fileListPage";
    public static final String PROGRESS_PAGE_ID = "progressPage";

    private final Map<String, String> remoteProjectNames;

    private IncomingProjectNegotiation process;
    private JID peer;
    private List<FileList> fileLists;

    /**
     * projectID => Project
     */
    private Map<String, IProject> remoteProjects;

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

    private final Wizard wizard;
    private final SelectProjectPage selectProjectPage;
    private final ProgressPage progressPage;
    private final ProgressPageWithInfo fileListPage;

    private final PageActionListener selectProjectsPageListener = new PageActionListener() {
        @Override
        public void back() {

        }

        @Override
        public void next() {
            //FIXME: Add support for different name
            if (selectProjectPage.isNewProjectSelected())
                wizard.setNextPage(progressPage);

            for (Map.Entry<String, String> entry : remoteProjectNames.entrySet()) {
                final String projectID = entry.getKey();
                final String projectName = entry.getValue();
                IProject project = saros.getWorkspace().getProject(projectName);

                remoteProjects.put(projectID, project);
            }

            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    createAndOpenProjects(remoteProjects);
                    if (!selectProjectPage.isNewProjectSelected()) {
                        showFilesChangedPage(remoteProjects);
                    } else {
                        triggerProjectNegotiation();
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

    private final PageActionListener fileListPageListener = new PageActionListener() {
        @Override
        public void back() {

        }

        @Override
        public void next() {
            triggerProjectNegotiation();
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
        remoteProjectNames = projectNames;
        remoteProjects = new HashMap<String, IProject>();

        String prjName = projectNames.values().iterator().next();

        HeaderPanel headerPanel = new HeaderPanel(
            Messages.EnterProjectNamePage_title2, "");
        wizard = new Wizard(Messages.AddProjectToSessionWizard_title,
            headerPanel);

        selectProjectPage = new SelectProjectPage(SELECT_PROJECT_PAGE_ID,
            prjName, prjName, workspace.getLocation().toOSString());
        selectProjectPage.addPageListener(selectProjectsPageListener);
        wizard.registerPage(selectProjectPage);

        fileListPage = new ProgressPageWithInfo(FILE_LIST_PAGE_ID, "Local file changes:");
        fileListPage.addPageListener(fileListPageListener);
        wizard.registerPage(fileListPage);

        progressPage = new ProgressPage(PROGRESS_PAGE_ID);
        wizard.registerPage(progressPage);

        wizard.create();

        process.setProjectInvitationUI(this);
    }

    public void cancelWizard(JID peer, String errorMsg,
                             ProcessTools.CancelLocation type) {
        String message = "Wizard cancelled ";
        message += type.equals(ProcessTools.CancelLocation.LOCAL) ? "locally " : "remotely ";
        message += "by " + peer.toString();
        DialogUtils.showInfo(wizard, message, message
            + errorMsg != null ? "\n\n" +  errorMsg : "");
        wizard.close();
    }

    private void triggerProjectNegotiation() {
        final IProgressMonitor monitor = progressPage
                .getProgressMonitor(true, true);
        ProjectNegotiation.Status status =  process.run(remoteProjects, monitor, false);

        if (status != ProjectNegotiation.Status.OK) {
            DialogUtils.showError(wizard, "Error during project negotiation", "The project could not be shared");
        } else {
            NotificationPanel.showNotification("Project shared", "Project successfully shared");
        }
    }

    private void showFilesChangedPage(Map<String, IProject> projectMapping) {

        IProgressMonitor monitor = fileListPage.getProgressMonitor(true, false);
        final Map<String, FileListDiff> modifiedResources = getModifiedResourcesFromMofifiableProjects(projectMapping, monitor);

        boolean empty = true;
        for (String key : modifiedResources.keySet()) {
            fileListPage.addLine("Project [" + key + "]:");
            FileListDiff diff = modifiedResources.get(key);
            for (String path : diff.getAlteredPaths()) {
                fileListPage.addLine("changed: " + path);
                empty = false;
            }

            for (String path : diff.getRemovedPaths()) {
                fileListPage.addLine("removed: " + path);
                empty = false;
            }

            for (String path : diff.getAddedPaths()) {
                fileListPage.addLine("added: " + path);
                empty = false;
            }
        }
        if (empty) {
            fileListPage.addLine("No files have to be modified.");
        }
        monitor.setTaskName("");
        monitor.done();
    }

    private Map<String, FileListDiff> getModifiedResourcesFromMofifiableProjects(Map<String, IProject> projectMapping, IProgressMonitor monitor) {
        monitor.setTaskName("Calculating changed files...");

        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        modifiedProjects.putAll(getModifiedProjects(projectMapping));
        try {
            modifiedResources
                .putAll(getModifiedResources(modifiedProjects, monitor));
        } catch (IOException e) {
            LOG.error(e);
            DialogUtils.showError(wizard, "Calculation error",
                    "Error while calculating modified resources: " + e
                            .getMessage()
            );
            wizard.close();
        }
        return modifiedResources;
    }


    /**
     * Returns a project mapping that contains all projects that will be
     * modified on synchronization.
     */
    private Map<String, IProject> getModifiedProjects(
        Map<String, IProject> projectMapping) {
        Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {
            //TODO: Add check for non-overwritable projects
            modifiedProjects.put(entry.getKey(), entry.getValue());
        }

        return modifiedProjects;
    }

    private void createAndOpenProjects(Map<String, IProject> projectMapping) {

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
                    .showError(wizard, "Could not create project.",
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

        final ISarosSession session = sessionManager.getSarosSession();

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

            try {
                if (session.isShared(project)) {
                    List<IResource> eclipseResources = session
                        .getSharedResources(project);

                    FileList sharedFileList = FileListFactory
                        .createFileList(project, eclipseResources,
                            checksumCache, null,
                            new SubProgressMonitor(monitor, 1,
                                SubProgressMonitor.SUPPRESS_SETTASKNAME));

                    remoteFileList.getPaths().addAll(sharedFileList.getPaths());
                } else {
                    subMonitor.worked(1);
                }

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
