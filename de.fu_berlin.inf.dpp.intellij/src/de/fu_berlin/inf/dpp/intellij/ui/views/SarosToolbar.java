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

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.*;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.SarosActionFactory;
import de.fu_berlin.inf.dpp.intellij.ui.actions.events.SarosActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.CommonButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.ConnectButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.ConsistencyButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.FollowButton;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Saros core panel toolbar
 */
public class SarosToolbar
{
    public static final String ADD_CONTACT_ICON_PATH = "icons/elcl16/buddy_add_tsk.png";
    public static final String OPEN_REFS_ICON_PATH = "icons/etool16/test_con.gif";
    public static final String LEAVE_SESSION_ICON_PATH = "icons/elcl16/project_share_leave_tsk.png";

    private Map<String, JButton> toolbarButtons = new HashMap<String, JButton>();

    private Saros saros = Saros.instance();

    private SarosMainPanelView sarosMainView;
    private JToolBar jToolBar;

    private SarosActionListener toolbarActionListener = new SarosActionListener()
    {
        @Override
        public void actionStarted(ISarosAction action)
        {

        }

        @Override
        public void actionFinished(ISarosAction action)
        {
            initButtons();
        }
    };

    private SarosActionListener treeActionListener = new SarosActionListener()
    {
        @Override
        public void actionStarted(ISarosAction action)
        {

        }

        @Override
        public void actionFinished(ISarosAction action)
        {
            if (action instanceof IConnectionAction)
            {

                final SarosTreeView sarosTree = sarosMainView.getSarosTree();
                if (saros.isConnected())
                {
                    sarosTree.renderConnected();
                }
                else
                {
                    sarosTree.renderDisconnected();
                }

                Runnable run = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        JTree jTree = sarosTree.getRootTree().getJtree();
                        DefaultTreeModel model = (DefaultTreeModel) (jTree.getModel());
                        model.reload();

                        jTree.expandRow(2);
                    }
                };

                UIUtil.invokeAndWaitIfNeeded(run);
            }
        }
    };

    /**
     * @param mainPanel
     */
    public SarosToolbar(SarosMainPanelView mainPanel)
    {
        this.jToolBar = create(mainPanel);
        mainPanel.getParent().add(this.jToolBar, BorderLayout.NORTH);
    }

    /**
     * @param parent
     * @return
     */
    private JToolBar create(SarosMainPanelView parent)
    {
        this.sarosMainView = parent;

        jToolBar = new JToolBar("Saros IntelliJ toolbar");
        jToolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        addToolbarButtons();

        parent.add(jToolBar, BorderLayout.PAGE_START);

        initButtons();

        return jToolBar;
    }

    /**
     *
     */
    protected void addToolbarButtons()
    {

        ConnectButton connectionButton = new ConnectButton();
        //triggers tree changes
        connectionButton.getConnectAction().addActionListener(treeActionListener);
        connectionButton.getDisconnectAction().addActionListener(treeActionListener);
        //triggers toolbar buttons enable/disable
        connectionButton.getConnectAction().addActionListener(toolbarActionListener);
        connectionButton.getDisconnectAction().addActionListener(toolbarActionListener);

        jToolBar.add(connectionButton);
        toolbarButtons.put(connectionButton.getConnectAction().getActionName(), connectionButton);
        toolbarButtons.put(connectionButton.getDisconnectAction().getActionName(), connectionButton);

        //add contact button
        addNavigationButton(NewContactAction.NAME, "Add contact to session", ADD_CONTACT_ICON_PATH, "addContact");

        //preferences button
        addNavigationButton(NotImplementedAction.actions.preferences.name(), "Open preferences", OPEN_REFS_ICON_PATH, "preferences");

        //follow button
        //addNavigationButton(FollowModeAction.NAME, "Enter follow mode", "followmode", "follow");
        FollowButton followButton = new FollowButton();
        toolbarButtons.put(followButton.getActionCommand(), followButton);
        jToolBar.add(followButton);

        //reload button
        // addNavigationButton(NotImplementedAction.actions.reload.name(), "Reload", "images/btn/reload.png", "reload");
        ConsistencyButton consistencyButton = new ConsistencyButton();
        toolbarButtons.put(consistencyButton.getActionCommand(), consistencyButton);
        jToolBar.add(consistencyButton);

        //session leave button
        ISarosAction actionLeave = SarosActionFactory.getAction(LeaveSessionAction.NAME);
        addNavigationButton(actionLeave.getActionName(), "Leave session", LEAVE_SESSION_ICON_PATH, "leave");
        actionLeave.addActionListener(treeActionListener);
        actionLeave.addActionListener(toolbarActionListener);

    }

    /**
     * @param action
     * @param toolTipText
     * @param iconPath
     * @param altText
     */
    private void addNavigationButton(String action, String toolTipText, String iconPath, String altText)
    {
        //Create and initialize the button.
        JButton button = new CommonButton(action, toolTipText, iconPath, altText);
        toolbarButtons.put(button.getActionCommand(), button);
        jToolBar.add(button);
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

    public Map<String, JButton> getToolbarButtons()
    {
        return toolbarButtons;
    }

    public JToolBar getJToolBar()
    {
        return jToolBar;
    }
}
