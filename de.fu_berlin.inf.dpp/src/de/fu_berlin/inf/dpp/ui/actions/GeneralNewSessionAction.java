/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Abstract base class for an action to start sharing a project (a "session").
 */
@Component(module = "action")
public abstract class GeneralNewSessionAction implements IObjectActionDelegate {

    private static final Logger log = Logger
        .getLogger(GeneralNewSessionAction.class.getName());

    protected List<IProject> selectedProjects;

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected Saros saros;

    @Inject
    protected PreferenceUtils preferenceUtils;

    public GeneralNewSessionAction() {
        super();
        Saros.injectDependenciesOnly(this);
    }

    /**
     */
    public void runNewSession(List<IResource> resource) {
        try {
            boolean running = sessionManager.getSarosSession() != null;
            boolean connected = saros.isConnected();

            if (!connected) {
                saros.connect(false);
            }
            if (running) {
                // sessionManager.openInviteDialog(null);
                if (this.selectedProjects.size() > 1) {
                    String message = "You selected the Projects:\n";
                    for (IProject p : this.selectedProjects) {
                        message += "- " + p.getName() + "\n";
                    }
                    message += "\nSharing multiple projects is not supported yet.\nIf you continue only project "
                        + this.selectedProjects.get(0) + " will be shared.";
                    if (!Util.popUpYesNoQuestion("Multiple Projects marked",
                        message, false)) {
                        return;
                    }
                }
                List<IProject> projectsToShare = new ArrayList<IProject>();
                projectsToShare.add(this.selectedProjects.get(0));
                sessionManager.addProjectsToSession(projectsToShare);
            } else {
                if (this.selectedProjects.size() > 1) {
                    String message = "You selected the Projects:\n";
                    for (IProject p : this.selectedProjects) {
                        message += "- " + p.getName() + "\n";
                    }
                    message += "\nSharing multiple projects is not supported yet.\nIf you continue only project "
                        + this.selectedProjects.get(0) + " will be shared.";
                    if (!Util.popUpYesNoQuestion("Multiple Projects marked",
                        message, false)) {
                        return;
                    }
                }
                List<IProject> projectsToShare = new ArrayList<IProject>();
                projectsToShare.add(this.selectedProjects.get(0));
                sessionManager.startSession(projectsToShare, resource);
                sessionManager.openInviteDialog(preferenceUtils
                    .getAutoInviteUsers());
            }
        } catch (final XMPPException e) {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    ErrorDialog.openError(EditorAPI.getShell(),
                        "Error Starting Session",
                        "Session could not be started", new Status(
                            IStatus.ERROR, "de.fu_berlin.inf.dpp",
                            IStatus.ERROR, e.getMessage(), e));
                }
            });
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selectedProjects = getProjects(selection);
        action.setEnabled(true);
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // We deal with everything in selectionChanged
    }

    protected List<IProject> getProjects(ISelection selection) {
        List<IProject> result = new ArrayList<IProject>();
        IStructuredSelection elements = ((IStructuredSelection) selection);
        for (Object element : elements.toArray()) {
            if (element instanceof IResource
                && (!result.contains(((IResource) element).getProject()))) {
                result.add(((IResource) element).getProject());
            }
        }

        return result;
    }
}
