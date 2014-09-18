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

package de.fu_berlin.inf.dpp.intellij.ui.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.fs.FolderImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Saros action group for the pop-up menu when right-clicking on a module.
 */
public class SarosFileShareGroup extends ActionGroup {
    private static final Logger LOG = Logger.getLogger(SarosFileShareGroup.class);

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private XMPPConnectionService connectionService;

    @Override
    public void actionPerformed(AnActionEvent e) {
        //do nothing when menu pops-up
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        //the object has to be initialized here, because it is created before
        //{@link de.fu_berlin.inf.dpp.core.Saros}.
        if (sessionManager == null && connectionService == null) {
            SarosPluginContext.initComponent(this);
        }

        if (e == null || !Saros.isInitialized()
                || sessionManager.getSarosSession() != null) {
            return new AnAction[0];
        }

        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project ideaProject = e.getData(CommonDataKeys.PROJECT);
        if (virtualFile == null || ideaProject == null) {
            return new AnAction[0];
        }

        if (!virtualFile.isDirectory()) {
            return new AnAction[0];
        }

        ProjectImp project = getProjectFromVirtFile(virtualFile, ideaProject);
        FolderImp resFolder = new FolderImp(project, new File(virtualFile.getPath()));
        //Holger: Disable partial sharing for the moment, until the need arises
        if (!isCompletelyShared(project, resFolder)) {
            return new AnAction[0];
        }

        List<AnAction> list = new ArrayList<AnAction>();
        Roster roster = connectionService.getRoster();
        for (RosterEntry rosterEntry : roster.getEntries()) {
                Presence presence = roster.getPresence(rosterEntry.getUser());

                if (presence.getType() == Presence.Type.available) {
                    list.add(new ShareWithUserAction(new JID(rosterEntry.getUser())));
                }
        }

        return list.toArray(new AnAction[]{});
    }


    public class ShareWithUserAction extends AnAction {

        private final JID userJID;
        private final String title;

        public ShareWithUserAction(JID user) {
            super(user.getName(), null, IconManager.CONTACT_ONLINE_ICON);
            userJID = user;
            title = user.getName();
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            VirtualFile virtFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (virtFile == null) {
                return;
            }

            try {
                ProjectImp project = getProjectFromVirtFile(virtFile, e.getProject());

                List<IResource> resources = new ArrayList<IResource>();
                //We allow only completely shared projects, so no need to check
                //for partially shared ones.
                project.refreshLocal();
                resources.add(project);

                List<JID> contacts = Arrays.asList(userJID);

                CollaborationUtils.startSession(resources, contacts);
            } catch (IOException e1) {
                LOG.error("could not share project with user due to", e1);
            }
        }

        public String toString() {
            return super.toString() + " " + title;
        }
    }

    private ProjectImp getProjectFromVirtFile(VirtualFile virtFile, Project project) {
        Module module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(virtFile);
        String moduleName = null;
        if (module != null) {
            moduleName = module.getName();
        } else {
            //FIXME: Find way to select moduleName for non-module based IDEAs
            //(Webstorm)
        }
        return new ProjectImp(project, moduleName, new File(project.getBasePath() + "/" + moduleName));
    }

    private boolean isCompletelyShared(ProjectImp project,
        FolderImp resFolder) {
        return
            resFolder.getFullPath().equals(project.getFullPath());
    }
}
