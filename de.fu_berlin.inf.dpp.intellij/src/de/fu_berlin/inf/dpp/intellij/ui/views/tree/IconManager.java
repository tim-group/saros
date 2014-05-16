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

import de.fu_berlin.inf.dpp.intellij.util.PluginResourceLocator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.4
 * Time: 09.07
 */

public class IconManager
{
   public  static final ImageIcon sessionsIcon;
   public static final ImageIcon contactOnlineIcon;
   public static final ImageIcon contactOfflineIcon;
   public static final ImageIcon contactsIcon;

    /**
     * Creates icons
     */
    static
    {
        sessionsIcon = new ImageIcon(PluginResourceLocator.getTreeImageUrl("project_share_tsk"), "sessions");
        contactOnlineIcon = new ImageIcon(PluginResourceLocator.getTreeImageUrl("buddy_saros_obj"), "contactOnLine");
        contactOfflineIcon = new ImageIcon(PluginResourceLocator.getTreeImageUrl("buddy_offline_obj"), "contactOffLine");
        contactsIcon = new ImageIcon(PluginResourceLocator.getTreeImageUrl("group"), "contacts");
    }

    /**
     *
     * @param path
     * @return
     */
    public static ImageIcon getIcon(String path)
    {
       return new ImageIcon(PluginResourceLocator.getResourceUrl(path));
    }

    /**
     * Starts listeners
     *
     * @param tree
     */
    protected void setIcons(JTree tree)
    {

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
        {
            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean selected, boolean expanded,
                    boolean isLeaf, int row, boolean focused)
            {
                Component c = super.getTreeCellRendererComponent(tree, value,
                        selected, expanded, isLeaf, row, focused);

                TreePath path = tree.getPathForRow(row);
                if (path != null)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                    if (node != null)
                    {
                        if (node instanceof RootTree)
                        {
                            setIcon(null);
                        }
                        else if (node instanceof SessionTree)
                        {
                            setIcon(sessionsIcon);
                        }
                        else if (node instanceof ContactTree)
                        {
                            setIcon(contactsIcon);
                        }
                        else
                        {
                            if (node.getUserObject() instanceof AbstractTree.LeafInfo)
                            {
                                AbstractTree.LeafInfo info = (AbstractTree.LeafInfo) node.getUserObject();
                                if (info.getIcon() != null)
                                {
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
