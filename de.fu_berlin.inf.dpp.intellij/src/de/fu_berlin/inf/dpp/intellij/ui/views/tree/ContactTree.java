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


import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 08.53
 */

public class ContactTree extends AbstractTree implements IRosterListener
{
    public static final String TREE_TITLE = "Contacts";

    private RootTree rootTree;
    private Map<String, ContactInfo> contactMap = new HashMap<String, ContactInfo>();

    /**
     * @param parent
     */
    public ContactTree(RootTree parent)
    {
        super(parent);
        this.rootTree = parent;

        setUserObject(new AbstractTree.CategoryInfo(TREE_TITLE));

        create();

        //register listener
        //saros.getConnectionService().

    }

    protected void create()
    {

    }


    public void addContacts(final Collection<RosterEntry> entries)
    {

        removeAllChildren();
        DefaultMutableTreeNode node;

        //add contacts
        for (RosterEntry contactEntry : entries)
        {
            ContactInfo contactInfo = new ContactInfo(contactEntry);
            contactInfo.setIcon(IconManager.contactOfflineIcon);
            node = new DefaultMutableTreeNode(contactInfo);
            add(node);

            contactMap.put(contactInfo.getKey(), contactInfo);
        }
    }

    public void removeContacts()
    {
        removeAllChildren();
        contactMap.clear();
    }

    @Override
    public void rosterChanged(Roster roster)
    {
        // System.out.println("ContactTree.rosterChanged");
    }

    @Override
    public void entriesAdded(Collection<String> addresses)
    {
        //  System.out.println("ContactTree.entriesAdded");
    }

    @Override
    public void entriesUpdated(Collection<String> addresses)
    {
        //  System.out.println("ContactTree.entriesUpdated");
    }

    @Override
    public void entriesDeleted(Collection<String> addresses)
    {
        //  System.out.println("ContactTree.entriesDeleted");
    }

    @Override
    public void presenceChanged(Presence presence)
    {

        final String append = "/Saros";  //todo: why that needed?
        String user = presence.getFrom();
        if (user.endsWith(append))
        {
            user = user.substring(0, user.length() - append.length());
        }

        ContactInfo info = contactMap.get(user);
        if (info != null)
        {
            if (presence.getType() == Presence.Type.available)
            {
                info.setIcon(IconManager.contactOnlineIcon);
            }
            else
            {
                info.setIcon(IconManager.contactOfflineIcon);
            }

            Runnable action = new Runnable()
            {
                @Override
                public void run()
                {
                    rootTree.getJtree().collapseRow(2);
                    rootTree.getJtree().expandRow(2);
                }
            };

            UIUtil.invokeAndWaitIfNeeded(action);
        }


    }

    /**
     * Class to keep contact info
     */
    protected class ContactInfo extends LeafInfo
    {

        private String status;
        private RosterEntry rosterEntry;

        private ContactInfo(RosterEntry rosterEntry)
        {
            super(rosterEntry.getUser(), rosterEntry.getUser());
            this.rosterEntry = rosterEntry;
            this.status = rosterEntry.getStatus() == null ? null : rosterEntry.getStatus().toString();
        }

        public ContactInfo(String key, String title)
        {
            super(key, title);
        }

        public RosterPacket.ItemStatus getStatus()
        {
            return rosterEntry.getStatus();
        }

        public String toString()
        {
            return this.status == null ? title : title + " (" + status + ")";
        }
    }

}
