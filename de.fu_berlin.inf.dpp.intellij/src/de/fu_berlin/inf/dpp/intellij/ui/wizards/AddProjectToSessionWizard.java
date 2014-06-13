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

import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.intellij.exception.CoreException;
import de.fu_berlin.inf.dpp.core.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.core.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.monitor.*;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.IChecksumCache;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.ui.IAddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.core.ui.IEnterProjectNamePage;
import de.fu_berlin.inf.dpp.core.ui.IWizardDialogAccessible;
import de.fu_berlin.inf.dpp.core.ui.Messages;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.IFileEditorInput;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.*;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.PageActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.Wizard;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoWithProgressPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.SelectProjectPage;
import de.fu_berlin.inf.dpp.invitation.*;
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

public class AddProjectToSessionWizard implements IAddProjectToSessionWizard
{
    public static final String INFO_PAGE_ID = "infoPage";
    public static final String FILE_LIST_PAGE_ID = "fileListPage";
    public static final String PROGRESS_PAGE_ID = "progressPage";

    private static Logger log = Logger.getLogger(AddProjectToSessionWizard.class);

    protected IEnterProjectNamePage namePage; //todo

    protected IWizardDialogAccessible wizardDialog; //todo

    protected IncomingProjectNegotiation process;
    protected JID peer;
    protected List<FileList> fileLists;

    /**
     * projectID => projectName
     */
    protected Map<String, String> remoteProjectNames;

    @Inject
    protected IEditorAPI editorAPI;

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

    private Wizard wizard;
    private SelectProjectPage infoPage;
    private ProgressPage progressPage;
    private InfoWithProgressPage fileListPage;

    private PageActionListener infoPageListener = new PageActionListener()
    {
        @Override
        public void back()
        {

        }

        @Override
        public void next()
        {
            String newName = infoPage.getNewProjectName();
            boolean isExisting = false;
            if (newName == null)
            {
                newName = infoPage.getExistingProjectName();
                isExisting = true;
            }
            else
            {
                wizard.getWizardModel().setNextPage(progressPage);
            }

            String key = remoteProjectNames.keySet().iterator().next();
            remoteProjectNames.put(key, newName);

            final boolean checkFiles = isExisting;
            ThreadUtils.runSafeAsync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    if (checkFiles)
                    {
                        runCalculateChangedFiles();
                    }
                    else
                    {
                        runAddProject();
                    }
                }
            });

        }

        @Override
        public void cancel()
        {
            ThreadUtils.runSafeAsync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    process.localCancel("Not accepted", ProcessTools.CancelOption.NOTIFY_PEER);
                }

            });
            wizard.close();
        }
    };

    private PageActionListener fileListPageListener = new PageActionListener()
    {
        @Override
        public void back()
        {

        }

        @Override
        public void next()
        {
            ThreadUtils.runSafeAsync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    runAddProject();
                }
            });
        }

        @Override
        public void cancel()
        {
            ThreadUtils.runSafeAsync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    process.localCancel("Not accepted", ProcessTools.CancelOption.NOTIFY_PEER);
                }

            });
            wizard.close();
        }
    };

    public AddProjectToSessionWizard(IncomingProjectNegotiation process,
            JID peer, List<FileList> fileLists, Map<String, String> projectNames)
    {

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


    private void runCalculateChangedFiles()
    {

        IProgressMonitor monitor = fileListPage.getProgressMonitor(true, false);
        monitor.setTaskName("Calculating changed files...");

        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        final Map<String, IProject> sources = new HashMap<String, IProject>();
        for (FileList fList : fileLists)
        {
            String localProjectName = remoteProjectNames.get(fList.getProjectID());
            IProject localProject = workspace.getRoot().getProject(localProjectName);
            try
            {
                localProject.refreshLocal();
            }
            catch (IOException e)
            {
               log.error(e);
            }
            sources.put(fList.getProjectID(),localProject);
        }
        modifiedProjects.putAll(getModifiedProjects(sources));
        try
        {
            modifiedResources.putAll(calculateModifiedResources(modifiedProjects, monitor));
        }
        catch (CoreException e)
        {
            e.printStackTrace();      //todo
            DialogUtils.openErrorMessageDialog(wizard.getWizard(), e.getMessage(), "Error");
            wizard.close();

        }

        int writeOverCount = 0;
        for (String key : modifiedResources.keySet())
        {
            // String prjName = remoteProjectNames.get(key);
            fileListPage.addLine("Project [" + key + "]:");
            FileListDiff diff = modifiedResources.get(key);
            for (IPath path : diff.getAlteredPaths())
            {
                fileListPage.addLine("changed: " + path.toPortableString());
                writeOverCount++;
            }

            for (IPath path : diff.getRemovedPaths())
            {
                fileListPage.addLine("removed: " + path.toPortableString());

            }

            for (IPath path : diff.getAddedPaths())
            {
                fileListPage.addLine("added: " + path.toPortableString());

            }
        }

        monitor.setTaskName("File changes calculated");
        monitor.done();

        if (writeOverCount > 0)
        {
            SafeDialogUtils.showWarning(writeOverCount + " files local changes will be overwritten!", "Warning");
        }

    }


    public void runAddProject()
    {
        final IProgressMonitor monitor = progressPage.getProgressMonitor(true, true);
        process.accept(remoteProjectNames, monitor, false);
    }

    protected Component getShell()
    {
        return Saros.instance().getMainPanel();
    }

    public void setWizardDlg(IWizardDialogAccessible wizardDialog)
    {
        this.wizardDialog = wizardDialog;
    }

    private Collection<IEditorPart> getOpenEditorsForSharedProjects(
            Collection<IProject> projects)
    {

        List<IEditorPart> openEditors = new ArrayList<IEditorPart>();

        //todo
//        Set<IEditorPart> editors = EditorAPI.getOpenEditors();
//
//        for (IProject project : projects) {
//            for (IEditorPart editor : editors) {
//                if (editor.getEditorInput() instanceof IFileEditorInput) {
//                    IFile file = ((IFileEditorInput) editor.getEditorInput())
//                            .getFile();
//                    if (project.equals(file.getProject()))
//                        openEditors.add(editor);
//                }
//            }
//        }
        return openEditors;
    }


    @Override
    public void cancelWizard(JID peer, String errorMsg, ProcessTools.CancelLocation type)
    {
        System.out.println("AddProjectToSessionWizard.cancelWizard");
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean performFinish()
    {

        final Map<String, IProject> sources = new HashMap<String, IProject>();
        final Map<String, String> projectNames = new HashMap<String, String>();
        final boolean useVersionControl = false;//namePage.useVersionControl();

        for (FileList fList : this.fileLists)
        {
            sources.put(fList.getProjectID(),
                    namePage.getSourceProject(fList.getProjectID()));
            projectNames.put(fList.getProjectID(),
                    namePage.getTargetProjectName(fList.getProjectID()));
        }

        List<IProject> existingProjects = new ArrayList<IProject>();

        for (IProject project : sources.values())
        {
            if (project != null)
            {
                existingProjects.add(project);
            }
        }

        final Collection<IEditorPart> openEditors = getOpenEditorsForSharedProjects(existingProjects);

        final List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();

        boolean containsDirtyEditors = false;

        for (IEditorPart editor : openEditors)
        {
            if (editor.isDirty())
            {
                containsDirtyEditors = true;
                dirtyEditors.add(editor);
            }
        }

        if (containsDirtyEditors)
        {
            SWTUtils.runSafeSWTAsync(log, new Runnable()
            {
                @Override
                public void run()
                {
//                    if (AddProjectToSessionWizard.this.getShell().isDisposed())
//                    {
//                        return;
//                    }

                    int max = Math.min(20, dirtyEditors.size());
                    int more = dirtyEditors.size() - max;

                    List<String> dirtyEditorNames = new ArrayList<String>();

                    for (IEditorPart editor : dirtyEditors.subList(0, max))
                    {
                        dirtyEditorNames.add(editor.getTitle());
                    }

                    Collections.sort(dirtyEditorNames);

                    if (more > 0)
                    {
                        dirtyEditorNames.add(MessageFormat
                                .format(
                                        Messages.AddProjectToSessionWizard_unsaved_changes_dialog_more,
                                        more));
                    }

                    String allDirtyEditorNames = StringUtils.join(
                            dirtyEditorNames, ", ");

                    String dialogText = MessageFormat
                            .format(
                                    Messages.AddProjectToSessionWizard_unsaved_changes_dialog_text,
                                    allDirtyEditorNames);

                    boolean proceed = DialogUtils.openQuestionMessageDialog(
                            AddProjectToSessionWizard.this.getShell(),
                            Messages.AddProjectToSessionWizard_unsaved_changes_dialog_title,
                            dialogText);

                    if (proceed)
                    {
                        for (IEditorPart editor : openEditors)
                        {
                            editor.doSave(new NullProgressMonitor());
                        }
                    }
                }
            });

            return false;
        }

        /*
         * Ask the user whether to overwrite local resources only if resources
         * are supposed to be overwritten based on the synchronization options
         * and if there are differences between the remote and local project.
         */
        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        modifiedProjects.putAll(getModifiedProjects(sources));

        try
        {
            Job job = new Job("Calculate resources")
            {
                @Override
                protected IStatus run(IProgressMonitor monitor)
                {
                    try
                    {
                        modifiedResources.putAll(calculateModifiedResources(modifiedProjects, monitor));
                    }
                    catch (CoreException e)
                    {
                        e.printStackTrace();
                        return new Status(IStatus.ERROR);
                    }

                    return new Status(IStatus.OK);

                }
            };
            job.schedule();

//            getContainer().run(true, false, new IRunnableWithProgress()
//            {
//                @Override
//                public void run(IProgressMonitor monitor)
//                        throws InvocationTargetException, InterruptedException
//                {
//                    try
//                    {
//                        modifiedResources.putAll(calculateModifiedResources(
//                                modifiedProjects, monitor));
//                    }
//                    catch (Exception e)
//                    {
//                        throw new InvocationTargetException(e);
//                    }
//                }
//            });
        }
        catch (Exception e)
        {
            Throwable cause = e.getCause();

            if (cause instanceof CoreException)
            {
                MessageDialog.openError(getShell(),
                        "Error computing file list",
                        "Could not compute local file list: " + cause.getMessage());
            }
            else
            {
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
        for (IEditorPart editor : openEditors)
        {
            editorAPI.closeEditor(editor);
        }

        Job job = new Job("Synchronizing")
        {
            @Override
            protected IStatus run(IProgressMonitor monitor)
            {
                try
                {
                    ProjectNegotiation.Status status = process.accept(
                            projectNames, monitor, useVersionControl);

                    if (status != ProjectNegotiation.Status.OK)
                    {
                        return Status.CANCEL_STATUS;
                    }

                    SarosView
                            .showNotification(
                                    Messages.AddProjectToSessionWizard_synchronize_finished_notification_title,
                                    MessageFormat
                                            .format(
                                                    Messages.AddProjectToSessionWizard_synchronize_finished_notification_text,
                                                    StringUtils.join(projectNames.values(),
                                                            ", ")
                                            )
                            );

                }
                catch (Exception e)
                {
                    log.error(
                            "unkown error during project negotiation: "
                                    + e.getMessage(), e
                    );
                    return Status.CANCEL_STATUS;
                }
                finally
                {
                    SWTUtils.runSafeSWTAsync(log, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for (IEditorPart editor : openEditors)
                            {
                                if (((IFileEditorInput) editor.getEditorInput())
                                        .getFile().exists())
                                {
                                    editorAPI.openEditor(editor);
                                }
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
            Map<String, IProject> projectMapping)
    {
        Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet())
        {
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
            throws CoreException
    {
        Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        ISarosSession session = sessionManager.getSarosSession();

        // FIXME the wizard should handle the case that the session may stop in
        // the meantime !

        if (session == null)
        {
            throw new IllegalStateException("no session running");
        }

        ISubMonitor subMonitor = monitor.convert(
                "Searching for files that will be modified...",
                projectMapping.size() * 2);

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet())
        {

            String projectID = entry.getKey();
            IProject eclipseProject = entry.getValue();

            FileListDiff diff;

            try
            {
                if (!eclipseProject.isOpen())
                {
                    eclipseProject.open();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            FileList remoteFileList = process.getRemoteFileList(projectID);

            IProject project = ResourceAdapterFactory.create(eclipseProject);

            /*
             * do not refresh already partially shared projects as this may
             * trigger resource change events
             */
            try
            {
                if (!session.isShared(project))
                {
                    project.refreshLocal();
                }


                if (session.isShared(project))
                {

                    List<IResource> eclipseResources = ResourceAdapterFactory
                            .convertBack(session.getSharedResources(project));

                    FileList sharedFileList = FileListFactory.createFileList(
                            eclipseProject, eclipseResources, checksumCache, true,
                            subMonitor.newChild(1, ISubMonitor.SUPPRESS_ALL_LABELS));

                    // FIXME FileList objects should be immutable after creation
                    remoteFileList.getPaths().addAll(sharedFileList.getPaths());
                }
                else
                {
                    subMonitor.worked(1);
                }

                diff = FileListDiff.diff(FileListFactory.createFileList(
                                eclipseProject, null, checksumCache, true,
                                subMonitor.newChild(1, ISubMonitor.SUPPRESS_ALL_LABELS)),
                        remoteFileList
                );


                if (process.isPartialRemoteProject(projectID))
                {
                    diff.clearRemovedPaths();
                }

                if (!diff.getRemovedPaths().isEmpty()
                        || !diff.getAlteredPaths().isEmpty())
                {
                    modifiedResources.put(eclipseProject.getName(), diff);
                }

            }
            catch (IOException e)
            {
                log.warn("could not refresh project: " + project, e);
            }
        }
        return modifiedResources;
    }


}
