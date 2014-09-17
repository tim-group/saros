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
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.actions.ConnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.DisconnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.NotImplementedAction;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.ConnectButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.ConsistencyButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.FollowButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.LeaveSessionButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.SimpleButton;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.picocontainer.annotations.Inject;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Saros toolbar.
 */
public class SarosToolbar {
    public static final String ADD_CONTACT_ICON_PATH = "icons/elcl16/buddy_add_tsk.png";
    public static final String OPEN_REFS_ICON_PATH = "icons/etool16/test_con.gif";

    //Convenience field for accessing buttons by action name.
    private final Map<String, JButton> toolbarButtons = new HashMap<String, JButton>();

    private final SarosMainPanelView sarosMainView;
    private final JToolBar jToolBar;

    @Inject
    private XMPPConnectionService connectionService;

    private final ActionListener treeActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent action) {
            if (action.getSource() instanceof ConnectServerAction
                || action.getSource() instanceof DisconnectServerAction) {

                final SarosTreeView sarosTree = sarosMainView.getSarosTree();
                if (connectionService.isConnected()) {
                    sarosTree.renderConnected();
                } else {
                    sarosTree.renderDisconnected();
                }

                Runnable updateTreeModel = new Runnable() {
                    @Override
                    public void run() {
                        JTree jTree = sarosTree.getRootTree().getJtree();
                        DefaultTreeModel model = (DefaultTreeModel) (jTree
                            .getModel());
                        model.reload();

                        jTree.expandRow(2);
                    }
                };

                UIUtil.invokeAndWaitIfNeeded(updateTreeModel);
            }
        }
    };

    public SarosToolbar(SarosMainPanelView mainPanel) {
        SarosPluginContext.initComponent(this);
        sarosMainView = mainPanel;

        jToolBar = new JToolBar("Saros IDEA toolbar");
        jToolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        addToolbarButtons();
        sarosMainView.getParent().add(jToolBar, BorderLayout.NORTH);
    }

    private void addToolbarButtons() {

        ConnectButton connectionButton = new ConnectButton();
        connectionButton.addActionListenerToActions(treeActionListener);
        addButton(connectionButton);
        toolbarButtons
            .put(DisconnectServerAction.NAME,
                connectionButton);

        addButton(
            new SimpleButton(new NotImplementedAction("addContact"), "Add contact to list",
                ADD_CONTACT_ICON_PATH, "addContact")
        );

        addButton(new SimpleButton(new NotImplementedAction("preferences"),
                "Open preferences", OPEN_REFS_ICON_PATH, "preferences")
        );

        addButton(new FollowButton());

        addButton(new ConsistencyButton());

        addButton(new LeaveSessionButton());
    }

    /**
     * Adds a button to the jToolBar and to the toolbarButtons.
     */
    private void addButton(JButton button) {
        toolbarButtons.put(button.getActionCommand(), button);
        jToolBar.add(button);
    }
}
