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
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.fs.PathImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.intellij.ui.resource.IconManager;
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
 * Contact pop-up menu
 */
class ContactPopMenu extends JPopupMenu {

    private static final Logger LOG = Logger.getLogger(ContactPopMenu.class);

    @Inject
    protected Saros saros;

    private ContactTree.ContactInfo contactInfo;

    private ModuleManager moduleManager;

    public ContactPopMenu(ContactTree.ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
        SarosPluginContext.initComponent(this);
        this.moduleManager = ModuleManager.getInstance(saros.getProject());

        JMenu menuShareProject = new JMenu("Work together on...");
        menuShareProject.setIcon(IconManager.SESSIONS_ICON);

        if (saros.getProject() != null) {
            JMenuItem projectItem = new JMenuItem(saros.getProject().getName());
            projectItem
                .addActionListener(new ShareProjectAction(saros.getProject()));

            menuShareProject.add(projectItem);

            //add sub-projects
            Module[] modules = moduleManager.getModules();
            //  if (modules.length > 0)
            if (false) {
                menuShareProject.addSeparator();

                for (Module module : modules) {

                    if (saros.getProject().getName()
                        .equalsIgnoreCase(module.getName())) {
                        continue;
                    }

                    projectItem = new JMenuItem(module.getName());
                    projectItem.addActionListener(
                        new ShareProjectAction(saros.getProject(), module));

                    menuShareProject.add(projectItem);
                }

            } else {
                //TODO: php mode: list of files and dirs
                menuShareProject.addSeparator();
                File dir = new File(saros.getProject().getBasePath());
                for (File myDir : dir.listFiles()) {
                    if (myDir.getName().startsWith(".") || myDir.isFile()) {
                        continue;
                    }

                    String name = myDir.isFile() ?
                        myDir.getName() :
                        "/" + myDir.getName();
                    projectItem = new JMenuItem(name);
                    projectItem
                        .addActionListener(new ShareDirectoryAction(myDir));

                    menuShareProject.add(projectItem);
                }
            }

        }

        add(menuShareProject);
        addSeparator();

        JMenuItem menuItemOpenChart = new JMenuItem("Open chart");
        menuItemOpenChart.addActionListener(new OpenChartAction());
        add(menuItemOpenChart);

        JMenuItem menuItemDelete = new JMenuItem("Delete");
        menuItemDelete.addActionListener(new DeleteContactAction());
        add(menuItemDelete);

    }

    private class ShareProjectAction implements ActionListener {
        private Project project;
        private Module module;

        private ShareProjectAction(Module module) {
            this.module = module;
        }

        private ShareProjectAction(Project project) {
            this.project = project;
        }

        private ShareProjectAction(Project project, Module module) {
            this.project = project;
            this.module = module;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = module == null ? project.getName() : module.getName();
            //  String path = module == null ? project.getBasePath() : module.getModuleFile().getParent().getPath();    //todo
            String path = module == null ?
                project.getBasePath() + "/TestProject" :
                module.getProject().getBasePath() + "/" + module.getName();

            IResource proj = saros.getWorkspace().getRoot().getProject(name);

            List<IResource> resources = Arrays.asList(proj);
            JID user = new JID(contactInfo.getRosterEntry().getUser());
            List<JID> contacts = Arrays.asList(user);

            CollaborationUtils.startSession(resources, contacts);

        }
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

                if (dir.isDirectory()) {

                    IProject proj = saros.getWorkspace().getRoot().getProject(dir.getName());
                    proj.refreshLocal();

                    resources = Arrays.asList((IResource) proj);
                } else {
                    //todo: not working properly after sharing
                    IProject proj = saros.getWorkspace().getRoot().getProject(dir.getName());
                    IResource prjFile = proj.getFile(new PathImp(dir));
                    resources = Arrays.asList(prjFile);
                }

                JID user = new JID(contactInfo.getRosterEntry().getUser());
                List<JID> contacts = Arrays.asList(user);

                CollaborationUtils.startSession(resources, contacts);
            } catch (IOException exception) {
                LOG.error("could not share directory due to", exception);
            }
        }
    }

    private class DeleteContactAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //todo: implement it
        }
    }

    private class OpenChartAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //todo: implement it
        }
    }

}
