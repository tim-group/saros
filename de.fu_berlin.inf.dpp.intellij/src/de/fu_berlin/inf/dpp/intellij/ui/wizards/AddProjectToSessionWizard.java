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
import de.fu_berlin.inf.dpp.core.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.ProcessTools;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.IChecksumCache;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.ui.*;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.WizardDialogAccessible;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.SarosProgressMonitor;
import de.fu_berlin.inf.dpp.invitation.FileList;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 14.08
 */

public class AddProjectToSessionWizard extends JFrame implements IAddProjectToSessionWizard, IWizard
{

    private static Logger log = Logger.getLogger(AddProjectToSessionWizard.class);

    protected IEnterProjectNamePage namePage;
    protected IWizardDialogAccessible wizardDialog;
    protected IncomingProjectNegotiation process;
    protected JID peer;
    protected List<FileList> fileLists;
    protected boolean isExceptionCancel;
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

    public AddProjectToSessionWizard(IncomingProjectNegotiation process,
            JID peer, List<FileList> fileLists, Map<String, String> projectNames)
    {
        //todo: UI implementation
        System.out.println("AddProjectToSessionWizard.AddProjectToSessionWizard //todo");

        SarosPluginContext.initComponent(this);

        this.process = process;
        this.peer = peer;
        this.fileLists = fileLists;
        this.remoteProjectNames = projectNames;

        this.setName(Messages.AddProjectToSessionWizard_title);
        //setHelpAvailable(true);
        //setNeedsProgressMonitor(true);

        process.setProjectInvitationUI(this);


        final IncomingProjectNegotiation proc = process;
        final Map<String, String> prjNames = projectNames;
        final Component comp = Saros.instance().getMainPanel();

        final StringBuffer names = new StringBuffer();
        for (String name : proc.getProjectNames().values())
        {
            names.append(name);
            names.append("\n");
        }

        final String path = workspace.getPath().getAbsolutePath();

        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {

                // Messages.showCheckboxOkCancelDialog(comp, "Do you want to join session?", "Incomming session");
//                int n = JOptionPane.showConfirmDialog(
//                        comp,
//                        "Do you want to add projects?\n" + names + "\nTo workspace:\n" + path,
//                        "Add projects to session",
//                        JOptionPane.YES_NO_OPTION);


                String newName = JOptionPane.showInputDialog(
                        comp,
                        "Do you want to add module?\nTo workspace:\n" + path,
                        names
                );

                IProgressMonitor monitor = new SarosProgressMonitor(comp, "Creating project: " + prjNames, "", 0, 100);
                // if (n == 0)
                if (newName != null && newName.trim().length() > 0)
                {
                    prjNames.put(prjNames.toString(),newName);
                    proc.accept(prjNames, monitor, false); //todo: make it by wizard!
                }
                else
                {
                    //clicked NO or closed dialog
                    proc.localCancel("Not accepted", ProcessTools.CancelOption.NOTIFY_PEER);
                }
            }
        });


        /** holds if the wizard close is because of an exception or not */
        isExceptionCancel = false;

        this.namePage = new EnterProjectNamePage();    //todo
        this.wizardDialog = new WizardDialogAccessible(this, this);//todo


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

    @Override
    public void setBlockOnOpen(boolean blockOnOpen)
    {

    }

    @Override
    public void setHelpAvailable(boolean helpAvailable)
    {

    }
}
