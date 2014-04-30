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

package de.fu_berlin.inf.dpp.intellij.ui.views;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.*;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.events.SarosActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.SarosActionFactory;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.CommonButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.ConnectButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.FollowButton;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Saros core panel toolbar
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class SarosToolbar implements SarosActionListener
{

    private Map<String, JButton> toolbarButtons = new HashMap<String, JButton>();

    private Project project;
    private Saros saros;

    private SarosMainPanelView sarosMainView;
    private JToolBar toolBar;


    public SarosToolbar()
    {
    }

    /**
     * @param parent
     */
    public SarosToolbar(SarosMainPanelView parent)
    {

        this.toolBar = create(parent);


    }

    /**
     * @param parent
     * @return
     */
    protected JToolBar create(SarosMainPanelView parent)
    {
        this.sarosMainView = parent;
        this.saros = Saros.instance();

        toolBar = new JToolBar("Saros IntelliJ toolbar");
        toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        addToolbarButtons();

        parent.add(toolBar, BorderLayout.PAGE_START);

        initButtons();

        return toolBar;
    }

    /**
     *
     */
    protected void addToolbarButtons()
    {

        ConnectButton connectionButton = new ConnectButton();
        //triggers tree changes
        connectionButton.getConnectAction().addActionListener(sarosMainView.getSarosTree());
        connectionButton.getDisconnectAction().addActionListener(sarosMainView.getSarosTree());
        //triggers toolbar buttons enable/disable
        connectionButton.getConnectAction().addActionListener(this);
        connectionButton.getDisconnectAction().addActionListener(this);

        toolBar.add(connectionButton);
        toolbarButtons.put(connectionButton.getConnectAction().getActionName(), connectionButton);
        toolbarButtons.put(connectionButton.getDisconnectAction().getActionName(), connectionButton);


        //add contact button
        addNavigationButton(NewContactAction.NAME, "Add contact to session", "buddy_add_tsk", "addContact");

        //preferences button
        addNavigationButton(NotImplementedAction.actions.preferences.name(), "Open preferences", "test_con", "preferences");

        //follow button
        //addNavigationButton(FollowModeAction.NAME, "Enter follow mode", "followmode", "follow");
        FollowButton followButton = new FollowButton();
        toolbarButtons.put(followButton.getActionCommand(), followButton);
        toolBar.add(followButton);

        //reload button
        addNavigationButton(NotImplementedAction.actions.reload.name(), "Reload", "reload", "reload");

        //session leave button
        ISarosAction actionLeave = SarosActionFactory.getAction(LeaveSessionAction.NAME);
        addNavigationButton(actionLeave.getActionName(), "Leave session", "project_share_leave_tsk", "leave");
        actionLeave.addActionListener(sarosMainView.getSarosTree());
        actionLeave.addActionListener(this);

    }

    /**
     * @param action
     * @param toolTipText
     * @param iconName
     * @param altText
     */
    private void addNavigationButton(String action, String toolTipText, String iconName, String altText)
    {
        //Create and initialize the button.
        JButton button = new CommonButton(action, toolTipText, iconName, altText);
        toolbarButtons.put(button.getActionCommand(), button);
        toolBar.add(button);
    }

    /**
     *
     */
    private void initButtons()
    {
        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {
                JButton btnConnect = toolbarButtons.get(ConnectServerAction.NAME);
                for (JButton button : toolbarButtons.values())
                {
                    if (btnConnect != button)
                    {
                        button.setEnabled(saros.isConnected());
                    }
                }
                btnConnect.setEnabled(true); //always enabled!
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);
    }


    public void setMainView(SarosMainPanelView mainView)
    {
        this.sarosMainView = mainView;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }


    @Override
    public void actionStarted(ISarosAction action)
    {

    }

    @Override
    public void actionFinished(ISarosAction action)
    {
        initButtons();
    }
}
