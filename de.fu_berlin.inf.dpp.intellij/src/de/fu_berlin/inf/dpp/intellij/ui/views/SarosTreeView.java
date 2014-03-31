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

import de.fu_berlin.inf.dpp.core.account.XMPPAccount;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.ContactTree;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.RootTree;
import de.fu_berlin.inf.dpp.intellij.ui.views.tree.SessionTree;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * Saros core panel tree list
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class SarosTreeView implements TreeExpansionListener, TreeSelectionListener, ISarosActionListener
{

    private Container parent;

    private Saros saros = Saros.instance();

    private RootTree rootTree;
    private SessionTree sessionTree;
    private ContactTree contactTree;

    /**
     *
     */
    public SarosTreeView()
    {
    }

    /**
     * @param parent
     */
    public SarosTreeView(Container parent)
    {
        this.parent = parent;
        this.parent.add(create());
    }

    /**
     * @return
     */
    public JTree create()
    {
        rootTree = new RootTree();

        sessionTree = new SessionTree(rootTree);
        contactTree = new ContactTree(rootTree);

        //todo: set nice icons

        return rootTree.getJtree();
    }


    public RootTree getRootTree()
    {
        return rootTree;
    }

    public SessionTree getSessionTree()
    {
        return sessionTree;
    }

    public ContactTree getContactTree()
    {
        return contactTree;
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event)
    {
        //  System.out.println("SarosTreeView.treeExpanded "+event);
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event)
    {
        //  System.out.println("SarosTreeView.treeCollapsed "+event);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        // System.out.println("SarosTreeView.valueChanged "+e);
    }

    @Override
    public void actionStarted(ISarosAction action)
    {
        // System.out.println("SarosTreeView.actionStarted");
    }

    @Override
    public void actionFinished(ISarosAction action)
    {

        if (saros.isConnected())
        {
            renderConnected();
        }
        else
        {
            renderDisconnected();
        }

        DefaultTreeModel model = (DefaultTreeModel) (rootTree.getJtree().getModel());
        model.reload();

        this.rootTree.getJtree().expandRow(2);
    }

    /**
     * Renders event connected
     */
    public void renderConnected()
    {
        XMPPAccount activeAccount = saros.getAccountStore().getActiveAccount();

        String rootText = activeAccount.getUsername() + "@" + activeAccount.getServer() + " (Connected)";
        rootTree.setTitle(rootText);

        //add contacts
        contactTree.addContacts(saros.getConnectionService().getRoster().getEntries());

        //add listener for on-line contacts
        saros.getConnectionService().getRoster().addRosterListener(contactTree);

    }

    /**
     * Renders event disconnected
     */
    public void renderDisconnected()
    {
        rootTree.setTitleDefault();

        contactTree.removeContacts();
        sessionTree.removeAllChildren();
    }

}
