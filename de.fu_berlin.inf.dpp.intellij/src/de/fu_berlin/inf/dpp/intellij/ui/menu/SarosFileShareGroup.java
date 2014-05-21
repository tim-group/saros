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

import com.intellij.find.FindProgressIndicator;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.IconManager;
import de.fu_berlin.inf.dpp.net.JID;
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


    public void actionPerformed(AnActionEvent e)
    {

    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e)
    {
        if (!Saros.isInitialized())
        {
            return new AnAction[0];
        }
        else
        {
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
            VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

            System.out.println("SarosFileShareGroup.actionPerformed " + file);

            try
            {
                File dir = new File(file.getPath());
                List<IResource> resources;

                if (dir.isDirectory())
                {
                    IProject proj = new ProjectImp(dir.getName(), dir);
                    proj.refreshLocal();

                    resources = Arrays.asList((IResource) proj);
                }
                else
                {
                    //todo: not working properly after sharing
                    ProjectImp proj = new ProjectImp(dir.getParentFile().getName(), dir.getParentFile());
                    IResource prjFile = new FileImp(proj, dir);
                    resources = Arrays.asList(prjFile);
                }

                List<JID> contacts = Arrays.asList(userJID);

                System.out.println("ShareProjectAction.actionPerformed START U=" + contacts + " P=" + resources);

                CollaborationUtils.startSession(resources, contacts);


            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }


        }

        public String toString()
        {
            return super.toString() + " " + title;
        }
    }
}
