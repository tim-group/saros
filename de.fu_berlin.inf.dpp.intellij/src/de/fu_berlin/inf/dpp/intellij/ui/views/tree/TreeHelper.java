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

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree helper
 */
public class TreeHelper
{
    /**
     * TreeNode -> TreePath
     *
     * @param treeNode
     * @return
     */
    public static TreePath getPath(TreeNode treeNode)
    {
        List<Object> nodes = new ArrayList<Object>();
        if (treeNode != null)
        {
            nodes.add(treeNode);
            treeNode = treeNode.getParent();
            while (treeNode != null)
            {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParent();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }
}


