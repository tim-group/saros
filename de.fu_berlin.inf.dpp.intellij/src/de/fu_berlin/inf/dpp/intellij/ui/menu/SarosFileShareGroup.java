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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.FolderImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.PathImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.IconManager;
import de.fu_berlin.inf.dpp.net.JID;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-09
 * Time: 10:58
 */

public class SarosFileShareGroup extends ActionGroup
{
    private static Logger log = Logger.getLogger(SarosFileShareGroup.class);

    public void actionPerformed(AnActionEvent e)
    {

    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e)
    {
        if (e == null || !Saros.isInitialized()
                || Saros.instance().getSessionManager().getSarosSession() != null)
        {
            return new AnAction[0];
        }
        else
        {
            VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            Project project = e.getData(CommonDataKeys.PROJECT);
            if (virtualFile == null || project == null
                    || virtualFile.equals(project.getBaseDir()))
            {
                return new AnAction[0];
            }

            List<AnAction> list = new ArrayList<AnAction>();
            for (JID user : Saros.instance().getMainPanel().getSarosTree().getContactTree().getOnLineUsers())
            {
                list.add(new ShareWithUser(user));
            }

            return list.toArray(new AnAction[]{});
        }

    }


    public class ShareWithUser extends AnAction
    {

        private JID userJID;
        private String title;


        public ShareWithUser(JID user)
        {
            super(user.getName(), null, IconManager.contactOnlineIcon);
            this.userJID = user;
            this.title = user.getName();
        }

        @Override
        public void actionPerformed(AnActionEvent e)
        {
            VirtualFile virtFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

            try
            {
                File file = new File(virtFile.getPath());
                ProjectImp project = locateProject(file);

                List<IResource> resources = new ArrayList<IResource>();


                if (file.isDirectory())
                {
                    //check if it is a real project
                    FolderImp resFolder = new FolderImp(project, file);
                    if (resFolder.getFullPath().segmentCount() == project.getFullPath().segmentCount())
                    {
                        project.refreshLocal();
                        resources.add(project);
                    }
                    else
                    {
                        resources.addAll(getParentFolders(project, project.getFullPath(), resFolder.getFullPath()));
                        loadChildResources(project, resFolder.toFile(), resources);
                    }

                }
                else
                {
                    FileImp resFile = new FileImp(project, file);
                    resources.addAll(getParentFolders(project, project.getFullPath(), resFile.getFullPath()));
                    resources.add(resFile);
                }

                List<JID> contacts = Arrays.asList(userJID);

                CollaborationUtils.startSession(resources, contacts);


            }
            catch (IOException e1)
            {
                log.trace(e1.getMessage(), e1);
            }
        }


        /**
         * Loads all down tree resourced
         *
         * @param project
         * @param file
         * @param resources
         * @throws IOException
         */
        private void loadChildResources(ProjectImp project, @NotNull File file, List<IResource> resources) throws IOException
        {
            if (file.isDirectory())
            {
                resources.add(new FolderImp(project, file));
                for (File f : file.listFiles())
                {
                    loadChildResources(project, f, resources);
                }
            }
            else
            {
                resources.add(new FileImp(project, file));
            }

        }


        /**
         * Load folders between project and file
         *
         * @param project
         * @param top
         * @param bottom
         * @return
         */
        private List<IResource> getParentFolders(ProjectImp project, IPath top, IPath bottom)
        {
            List<IResource> folders = new ArrayList<IResource>();
            File base = top.toFile();
            StringBuilder sbPath = new StringBuilder(base.getAbsolutePath());
            String[] segments = bottom.segments();
            for (int i = top.segmentCount(); i < segments.length - 1; i++)
            {
                sbPath.append(File.separator);
                sbPath.append(segments[i]);
                IFolder folder = new FolderImp(project, new File(sbPath.toString()));
                folders.add(folder);
            }

            return folders;
        }

        /**
         * @param resource
         * @return
         */
        private ProjectImp locateProject(File resource)
        {
            Project p = Saros.instance().getProject();
            IPath basePath = new PathImp(p.getBasePath());
            IPath resourcePath = new PathImp(resource);
            String[] resourceSegments = resourcePath.segments();
            int nameIndex = basePath.segmentCount();
            if (nameIndex >= resourceSegments.length)
            {
                nameIndex = --nameIndex;
            }

            String moduleName = resourceSegments[nameIndex];
            basePath = basePath.append(moduleName);
            return (ProjectImp)Saros.instance().getWorkspace().getRoot().addProject(moduleName,basePath.toFile());
        }


        public String toString()
        {
            return super.toString() + " " + title;
        }
    }
}
