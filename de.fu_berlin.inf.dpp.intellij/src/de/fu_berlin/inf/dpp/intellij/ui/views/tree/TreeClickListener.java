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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tree click listener for showing {@link ContactPopMenu} or {@link SessionPopMenu}.
 */
public class TreeClickListener extends MouseAdapter
{
    private JTree tree;

    public TreeClickListener(SarosTreeView treeView)
    {
        tree = treeView;
    }

    public void mousePressed(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            doPop(e);
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            doPop(e);
        }
    }

    private void doPop(MouseEvent e)
    {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath == null || selPath.getParentPath()==null)
        {
            return;
        }

        if (selPath.getLastPathComponent() instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                if (node.getUserObject() instanceof ContactTreeRootNode.ContactInfo)
                {
                    ContactTreeRootNode.ContactInfo contactInfo = (ContactTreeRootNode.ContactInfo) node.getUserObject();
                    if (contactInfo.isOnline())
                    {
                        ContactPopMenu menu = new ContactPopMenu(contactInfo);
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                if (node.getUserObject() instanceof SessionTreeRootNode.SessionInfo)
                {
                    SessionTreeRootNode.SessionInfo sessionInfo = (SessionTreeRootNode.SessionInfo) node.getUserObject();
                    SessionPopMenu menu = new SessionPopMenu(sessionInfo);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }

        }
    }
}
