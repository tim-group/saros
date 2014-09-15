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

import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Component;

/**
 * Root tree
 */
public class RootTree extends AbstractTree {
    public static final String SPACER = " ";
    public static final String TITLE_JABBER_SERVER = "XMPP/jabber server (Not connected)";

    protected JTree jtree;

    public RootTree() {
        setUserObject(new CategoryInfo(SPACER + TITLE_JABBER_SERVER
            + "                                                                    "));

        jtree = new JTree(this);
        jtree.getSelectionModel()
            .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        setTreeIcons(jtree);
    }

    public JTree getJtree() {
        return jtree;
    }

    public void setTitle(String title) {
        ((CategoryInfo) getUserObject()).title = SPACER + title;
    }

    public void setTitleDefault() {
        setTitle(TITLE_JABBER_SERVER);
    }

    /**
     * Starts listeners
     *
     * @param tree
     */
    protected void setTreeIcons(JTree tree) {

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
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
                        if (node instanceof RootTree) {
                            setIcon(null);
                        } else if (node instanceof SessionTree) {
                            setIcon(IconManager.SESSIONS_ICON);
                        } else if (node instanceof ContactTree) {
                            setIcon(IconManager.CONTACTS_ICON);
                        } else {
                            if (node
                                .getUserObject() instanceof AbstractTree.LeafInfo) {
                                AbstractTree.LeafInfo info = (AbstractTree.LeafInfo) node
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

        tree.setCellRenderer(renderer);

    }
}
