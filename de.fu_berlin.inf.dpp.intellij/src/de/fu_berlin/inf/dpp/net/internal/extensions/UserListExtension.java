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

package de.fu_berlin.inf.dpp.net.internal.extensions;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.User;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias(/* UserListStatusUpdate */"ULSUP")
public class UserListExtension extends SarosSessionPacketExtension
{

    public static final Provider PROVIDER = new Provider();

    private ArrayList<UserListEntry> userList = new ArrayList<UserListEntry>();

    public UserListExtension(String sessionID)
    {
        super(sessionID);
    }

    public void addUser(User user, long flags)
    {
        userList.add(UserListEntry.create(user, flags));
    }

    public List<UserListEntry> getEntries()
    {
        return userList;
    }

    public static class UserListEntry
    {
        public static final long USER_ADDED = 0x1L;
        public static final long USER_REMOVED = 0x2L;

        public long flags;
        public JID jid;
        public int colorID;
        public int favoriteColorID;
        public User.Permission permission;

        private static UserListEntry create(User user, long flags)
        {
            return new UserListEntry(user.getJID(), user.getColorID(),
                    user.getFavoriteColorID(), user.getPermission(), flags);
        }

        private UserListEntry(JID jid, int colorID, int favoriteColorID,
                User.Permission permission, long flags)
        {
            this.jid = jid;
            this.colorID = colorID;
            this.favoriteColorID = favoriteColorID;
            this.permission = permission;
            this.flags = flags;
        }
    }

    public static class Provider extends
            SarosSessionPacketExtension.Provider<UserListExtension>
    {

        private Provider()
        {
            super("ulsup", UserListExtension.class);
        }
    }
}
