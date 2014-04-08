package de.fu_berlin.inf.dpp.intellij.ui.menu;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.SarosComponent;
import de.fu_berlin.inf.dpp.intellij.ui.menu.core.AbstractMenuHandler;

/**
 * Created by IntelliJ IDEA.
 * User: r.kvietkauskas
 * Date: 14.3.13
 * Time: 10.28
 * To change this template use File | Settings | File Templates.
 */
public class AboutSarosHandler extends AbstractMenuHandler
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {

        Application app = ApplicationManager.getApplication();
        SarosComponent saros = app.getComponent(SarosComponent.class);


        Project project = e.getData(PlatformDataKeys.PROJECT);

        Messages.showMessageDialog(project, "Saros plugin for IntelliJ \nVersion 10.1.1\n\nDEVELOPMENT", "About Saros", Messages.getInformationIcon());

    }
}
