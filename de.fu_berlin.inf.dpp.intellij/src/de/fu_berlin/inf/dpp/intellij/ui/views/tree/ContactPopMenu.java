/*
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

package de.fu_berlin.inf.dpp.intellij.ui.views.tree;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Contact pop-up menu for selecting a project to share.
 */
class ContactPopMenu extends JPopupMenu {

    private static final Logger LOG = Logger.getLogger(ContactPopMenu.class);

    @Inject
    protected Saros saros;

    private ContactTreeRootNode.ContactInfo contactInfo;

    private ModuleManager moduleManager;

    public ContactPopMenu(ContactTreeRootNode.ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
        SarosPluginContext.initComponent(this);

        JMenu menuShareProject = new JMenu("Work together on...");
        menuShareProject.setIcon(IconManager.SESSIONS_ICON);

        if (saros.getProject() == null) {
            return;
        }

        ModuleManager moduleManager = ModuleManager.getInstance(saros.getProject());
        if (moduleManager != null) {
            for (Module module : moduleManager.getModules()) {

                if (saros.getProject().getName()
                    .equalsIgnoreCase(module.getName())) {
                    continue;
                }

                JMenuItem moduleItem = new JMenuItem(module.getName());
                moduleItem.addActionListener(
                    new ShareDirectoryAction(new File(module.getProject().getBasePath() + "/" + module.getName())));

                menuShareProject.add(moduleItem);
            }

        } else {
            File dir = new File(saros.getProject().getBasePath());
            for (File myDir : dir.listFiles()) {
                if (myDir.getName().startsWith(".") || myDir.isFile()) {
                    continue;
                }

                JMenuItem directoryItem = new JMenuItem(myDir.getName());
                directoryItem.addActionListener(new ShareDirectoryAction(myDir));
                menuShareProject.add(directoryItem);
            }
        }

        add(menuShareProject);
    }

    private class ShareDirectoryAction implements ActionListener {
        private final File dir;

        private ShareDirectoryAction(File dir) {
            this.dir = dir;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                List<IResource> resources;

                IProject proj = saros.getWorkspace()
                    .getProject(dir.getName());
                proj.refreshLocal();

                resources = Arrays.asList((IResource) proj);

                JID user = new JID(contactInfo.getRosterEntry().getUser());
                List<JID> contacts = Arrays.asList(user);

                CollaborationUtils.startSession(resources, contacts);
            } catch (IOException exception) {
                LOG.error("could not share directory due to", exception);
            }
        }
    }
}
