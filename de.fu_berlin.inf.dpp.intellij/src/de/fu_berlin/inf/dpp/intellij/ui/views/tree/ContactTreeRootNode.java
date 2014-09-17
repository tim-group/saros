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
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosTreeView;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.roster.IRosterListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.picocontainer.annotations.Inject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contact tree
 */
public class ContactTreeRootNode extends DefaultMutableTreeNode implements IRosterListener {
    public static final String TREE_TITLE = "Contacts";

    protected SarosTreeView treeView;
    private Map<String, ContactInfo> contactMap = new HashMap<String, ContactInfo>();
    private DefaultTreeModel treeModel;

    @Inject XMPPConnectionService connectionService;

    public ContactTreeRootNode(SarosTreeView treeView) {
        super(treeView);
        SarosPluginContext.initComponent(this);

        this.treeView = treeView;
        this.treeModel = (DefaultTreeModel) this.treeView.getModel();
        setUserObject(new CategoryInfo(TREE_TITLE));
    }

    public void createContactNodes() {
        removeAllChildren();
        DefaultMutableTreeNode node;

        Roster roster = connectionService.getRoster();
        //add contacts
        for (RosterEntry contactEntry : roster.getEntries()) {
            ContactInfo contactInfo = new ContactInfo(contactEntry);
            Presence presence = roster.getPresence(contactEntry.getUser());

            if (presence.getType() == Presence.Type.available) {
                contactInfo.setOnline(true);
            } else {
                contactInfo.setOnline(false);
            }

            node = new DefaultMutableTreeNode(contactInfo);

            add(node);

            contactMap.put(contactInfo.getKey(), contactInfo);
        }
    }

    public void showContact(final String key) {
        ContactInfo contactInfo = contactMap.get(key);
        if (contactInfo == null || !contactInfo.isHidden()) {
            return;
        }

        contactInfo.setHidden(false);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(contactInfo);

        add(node);
    }

    public void hideContact(final String key) {
        ContactInfo contactInfo = contactMap.get(key);
        if (contactInfo == null || contactInfo.isHidden()) {
            return;
        }

        contactInfo.setHidden(true);
        for (int i = 0; i < getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) getChildAt(
                i);
            if (contactInfo.equals(node.getUserObject())) {
                treeModel.removeNodeFromParent(node);
                node.setUserObject(null);
                break;
            }
        }

    }

    public void removeContacts() {
        removeAllChildren();
        contactMap.clear();
    }

    @Override
    public void rosterChanged(Roster roster) {

    }

    @Override
    public void entriesAdded(Collection<String> addresses) {

    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {

    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {

    }

    @Override
    public void presenceChanged(Presence presence) {
        String sUser = new JID(presence.getFrom()).getBareJID().toString();

        ContactInfo info = contactMap.get(sUser);
        if (info != null) {
            if (presence.getType() == Presence.Type.available) {
                info.setOnline(true);
            } else {
                info.setOnline(false);
            }

            Runnable action = new Runnable() {
                @Override
                public void run() {
                    treeView.collapseRow(2);
                    treeView.expandRow(2);
                }
            };

            UIUtil.invokeAndWaitIfNeeded(action);
        }

    }

    /**
     * Class to keep contact info
     */
    protected class ContactInfo extends LeafInfo {

        private String status;
        private RosterEntry rosterEntry;
        private boolean isOnline;
        private boolean isHidden = false;

        private ContactInfo(RosterEntry rosterEntry) {
            super(rosterEntry.getUser(), rosterEntry.getUser());
            this.rosterEntry = rosterEntry;
            this.status = rosterEntry.getStatus() == null ?
                null :
                rosterEntry.getStatus().toString();
        }

        public RosterPacket.ItemStatus getStatus() {
            return rosterEntry.getStatus();
        }

        public RosterEntry getRosterEntry() {
            return rosterEntry;
        }

        public boolean isOnline() {
            return isOnline;
        }

        public boolean isHidden() {
            return isHidden;
        }

        public void setHidden(boolean isHidden) {
            this.isHidden = isHidden;
        }

        public void setOnline(boolean isOnline) {
            this.isOnline = isOnline;
            if (isOnline) {
                setIcon(IconManager.CONTACT_ONLINE_ICON);
            } else {
                setIcon(IconManager.CONTACT_OFFLINE_ICON);
            }
        }

        public String toString() {
            return this.status == null ? title : title + " (" + status + ")";
        }
    }

}
