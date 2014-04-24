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

package de.fu_berlin.inf.dpp.intellij;


import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiManager;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.mock.MockInitializer;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.intellij.project.intl.WorkspaceIntl;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosMainPanelView;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Saros core panel tool window factory. Here is a starting point of IntelliJ plugin
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */

public class SarosToolWindowFactory implements ToolWindowFactory
{

    public static Project _project;

//    public static ProjectManager pm;
//    public static FileEditorManager em;
//    public static PsiManager psm;

    /**
     * Plugin starting point via IntelliJ
     *
     * @param project
     * @param toolWindow
     */
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow)
    {
        _project = project;
       /* pm =  ProjectManager.getInstance();
        em = FileEditorManager.getInstance(project);
        psm = PsiManager.getInstance(project);
        _project = project;

        File f = new File("c:\\Develop\\Saros\\idea\\test_projects\\testas1\\src\\test1.java");
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(f);
        System.out.println("DocumentProvider.connect>>>"+vf+" exist="+vf.exists());

        FileEditorManager  fem = FileEditorManager.getInstance(project);
        System.out.println("FollowModeAction.run DefaultProject="+project);
        System.out.println("FollowModeAction.run FileEditorManager="+ fem);

        fem.openFile(vf,true);

*/


        Saros.instance().start(true);
        SarosMainPanelView stw = new SarosMainPanelView(project, toolWindow);

        System.out.println("SarosToolWindowFactory.createToolWindowContent PATH=" + project.getBasePath() + " NAME=" + project.getName());

        //   System.out.println("SarosToolWindowFactory.createToolWindowContent>>>>"+project.getWorkspaceFile().getCanonicalPath());
        Workspace.instance().createWorkSpace(new File(project.getBasePath()));
      /*  try
        {
          //  WorkspaceIntl.instance().createWorkSpace(new File(project.getBasePath()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

    /**
     * Plugin starting point via shell (for testing only)
     *
     * @param args
     */
    public static void main(String[] args)
    {

        Saros.instance().start(true); // start saros


        MockInitializer.createProjects(); //todo

        SarosMainPanelView sarosMainView = new SarosMainPanelView();
        sarosMainView.setSize(new Dimension(800, 300));

        //  sarosPanel.pack();
        sarosMainView.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {

                Saros.instance().stop();
                System.exit(0);
            }
        });


        sarosMainView.setVisible(true);

    }

}
