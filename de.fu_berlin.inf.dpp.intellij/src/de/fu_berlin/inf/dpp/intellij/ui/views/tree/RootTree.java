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


import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 10.04
 */

public class RootTree extends AbstractTree
{
    public static final String SPACER = " ";
    public static final String TITLE_JABBER_SERVER = "XMPP/jabber server (Not connected)";

    protected JTree jtree;

    public RootTree()
    {
        setUserObject(new CategoryInfo(SPACER + TITLE_JABBER_SERVER + "                                                                    "));

        jtree = new JTree(this);
        jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        iconManager.setIcons(jtree);
    }

    public JTree getJtree()
    {
        return jtree;
    }

    public void setTitle(String title)
    {
        ((CategoryInfo) getUserObject()).title = SPACER + title;
    }

    public void setTitleDefault()
    {
        setTitle(TITLE_JABBER_SERVER);
    }


}
