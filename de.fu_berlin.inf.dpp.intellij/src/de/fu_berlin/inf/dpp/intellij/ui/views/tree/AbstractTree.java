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

import de.fu_berlin.inf.dpp.intellij.core.Saros;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 09.02
 */

public abstract class AbstractTree extends DefaultMutableTreeNode
{
    protected static final Logger log = Logger.getLogger(AbstractTree.class);


    protected final DefaultMutableTreeNode parent;
    protected final Saros saros;
    protected IconManager iconManager;


    protected AbstractTree()
    {
        this.saros = Saros.instance();
        this.parent = this;
        this.iconManager = new IconManager();
    }

    protected AbstractTree(DefaultMutableTreeNode parent)
    {
        this.parent = parent;
        this.saros = Saros.instance();
        this.iconManager = new IconManager();
        parent.add(this);
    }


    /**
     * Class to keep category information
     */
    class CategoryInfo
    {
        String title;
        private ImageIcon icon;

        CategoryInfo(String title)
        {
            this.title = title;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public ImageIcon getIcon()
        {
            return icon;
        }

        public void setIcon(ImageIcon icon)
        {
            this.icon = icon;
        }

        public String toString()
        {
            return title;
        }
    }

    /**
     * Default class to keep item info
     */
    class LeafInfo extends CategoryInfo
    {
        String key;

        LeafInfo(String key, String title)
        {
            super(title);
            this.key = key;
        }

        public String getKey()
        {
            return key;
        }

        public void setKey(String key)
        {
            this.key = key;
        }
    }
}
