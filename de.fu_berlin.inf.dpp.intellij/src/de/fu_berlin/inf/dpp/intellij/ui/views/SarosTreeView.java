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
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.ContactTree;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.RootTree;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.SessionTree;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.TreeClickListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.picocontainer.annotations.Inject;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

/**
 * Saros tree view for contacts and sessions.
 */
public class SarosTreeView {

    private RootTree rootTree;
    private SessionTree sessionTree;
    private ContactTree contactTree;

    @Inject
    private XMPPAccountStore accountStore;

    @Inject
    private XMPPConnectionService connectionService;

    public SarosTreeView() {
        SarosPluginContext.initComponent(this);
        create();
    }

    public JTree create() {
        rootTree = new RootTree();

        sessionTree = new SessionTree(rootTree);
        contactTree = new ContactTree(rootTree);

        this.rootTree.getJtree()
            .addMouseListener(new TreeClickListener(contactTree, sessionTree));

        //listeners
        TreeExpansionListener expansionListener = new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {

            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        };
        rootTree.getJtree().addTreeExpansionListener(expansionListener);

        TreeSelectionListener selectionListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

            }
        };
        rootTree.getJtree().addTreeSelectionListener(selectionListener);

        return rootTree.getJtree();
    }

    public RootTree getRootTree() {
        return rootTree;
    }

    public SessionTree getSessionTree() {
        return sessionTree;
    }

    public ContactTree getContactTree() {
        return contactTree;
    }

    /**
     * Displays the user@server (connected) string and populates the contact list.
     */
    protected void renderConnected() {
        XMPPAccount activeAccount = accountStore.getActiveAccount();

        String rootText =
            activeAccount.getUsername() + "@" + activeAccount.getServer()
                + " (Connected)";
        rootTree.setTitle(rootText);

        //add contacts
        contactTree.createContactNodes();

        //add listener for on-line contacts
        connectionService.getRoster().addRosterListener(contactTree);

        updateTree();
    }

    /**
     * clears the contact list and title.
     */
    protected void renderDisconnected() {
        rootTree.setTitleDefault();

        contactTree.removeContacts();
        sessionTree.removeAllChildren();

        updateTree();
    }

    private void updateTree() {
        Runnable updateTreeModel = new Runnable() {
            @Override
            public void run() {
                JTree jTree = getRootTree().getJtree();
                DefaultTreeModel model = (DefaultTreeModel) (jTree
                    .getModel());
                model.reload();

                jTree.expandRow(2);
            }
        };

        UIUtil.invokeAndWaitIfNeeded(updateTreeModel);
    }

}
