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

import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.picocontainer.annotations.Inject;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Component;

/**
 * Saros tree view for contacts and sessions.
 */
public class SarosTreeView extends Tree {

    private final SessionTreeRootNode sessionTreeRootNode;
    private final ContactTreeRootNode contactTreeRootNode;

    @Inject
    private XMPPAccountStore accountStore;

    @Inject
    private XMPPConnectionService connectionService;

    private final TreeCellRenderer renderer = new DefaultTreeCellRenderer() {
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
            Object value, boolean selected, boolean expanded,
            boolean isLeaf, int row, boolean focused) {
            Component c = super
                .getTreeCellRendererComponent(tree, value, selected,
                    expanded, isLeaf, row, focused);

            TreePath path = tree.getPathForRow(row);
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                    .getLastPathComponent();

                if (node != null) {
                    if (node instanceof SarosTreeRootNode) {
                        setIcon(null);
                    } else if (node instanceof SessionTreeRootNode) {
                        setIcon(IconManager.SESSIONS_ICON);
                    } else if (node instanceof ContactTreeRootNode) {
                        setIcon(IconManager.CONTACTS_ICON);
                    } else {
                        if (node
                            .getUserObject() instanceof LeafInfo) {
                            LeafInfo info = (LeafInfo) node
                                .getUserObject();
                            if (info.getIcon() != null) {
                                setIcon(info.getIcon());
                            }
                        }
                    }
                }
            }

            return c;
        }

    };

    public SarosTreeView() {
        super(new SarosTreeRootNode());
        SarosPluginContext.initComponent(this);
        sessionTreeRootNode = new SessionTreeRootNode(this);
        ((SarosTreeRootNode)getModel().getRoot()).add(sessionTreeRootNode);
        contactTreeRootNode = new ContactTreeRootNode(this);
        ((SarosTreeRootNode)getModel().getRoot()).add(contactTreeRootNode);
        addListeners();
        getSelectionModel()
            .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(renderer);
    }

    private void addListeners() {
        addMouseListener(new TreeClickListener(this));

        TreeExpansionListener expansionListener = new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {

            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        };
        addTreeExpansionListener(expansionListener);

        TreeSelectionListener selectionListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

            }
        };
        addTreeSelectionListener(selectionListener);
    }

    /**
     * Displays the 'user@domain (connected)' string and populates the contact list.
     */
    public void renderConnected() {
        XMPPAccount activeAccount = accountStore.getActiveAccount();

        String rootText =
            activeAccount.getUsername() + "@" + activeAccount.getDomain()
                + " (Connected)";
        getSarosTreeRootNode().setTitle(rootText);

        //add contacts
        contactTreeRootNode.createContactNodes();

        //add listener for on-line contacts
        connectionService.getRoster().addRosterListener(contactTreeRootNode);

        updateTree();
    }

    /**
     * Clears the contact list and title.
     */
    public void renderDisconnected() {
        getSarosTreeRootNode().setTitleDefault();

        contactTreeRootNode.removeContacts();
        sessionTreeRootNode.removeAllChildren();

        updateTree();
    }

    public void updateTree() {
        Runnable updateTreeModel = new Runnable() {
            @Override
            public void run() {
                DefaultTreeModel model = (DefaultTreeModel) (getModel());
                model.reload();

                expandRow(2);
            }
        };

        UIUtil.invokeAndWaitIfNeeded(updateTreeModel);
    }

    protected ContactTreeRootNode getContactTreeRootNode() {
        return contactTreeRootNode;
    }

    private SarosTreeRootNode getSarosTreeRootNode() {
        return (SarosTreeRootNode)getModel().getRoot();
    }
}
