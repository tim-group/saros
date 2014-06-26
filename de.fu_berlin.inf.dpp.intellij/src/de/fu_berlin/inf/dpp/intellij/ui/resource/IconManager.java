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

package de.fu_berlin.inf.dpp.intellij.ui.resource;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.net.URL;

/**
 * Class caches all icons used in application
 */
public class IconManager
{
    public static final Logger LOG = Logger.getLogger(IconManager.class);

    public static final ImageIcon SESSIONS_ICON = getIcon("icons/elcl16/project_share_tsk.png", "sessions");
    public static final ImageIcon CONTACT_ONLINE_ICON = getIcon("icons/obj16/buddy_saros_obj.png", "contactOnLine");
    public static final ImageIcon CONTACT_OFFLINE_ICON = getIcon("icons/obj16/buddy_offline_obj.png", "contactOffLine");
    public static final ImageIcon CONTACTS_ICON = getIcon("icons/obj16/group.png", "contacts");

    public static final ImageIcon FOLLOW_ICON = getIcon("icons/ovr16/followmode.png", "follow");

    public static final ImageIcon IN_SYNC_ICON = getIcon("icons/etool16/in_sync.png", "Files are consistent");
    public static final ImageIcon OUT_OF_SYNC_ICON = getIcon("icons/etool16/out_sync.png", "Files are NOT consistent");

    /**
     * Creates icon by image path
     *
     * @param path  Image path
     * @param descriprion  Icon description
     * @return  ImageIcon
     */
    public static ImageIcon getIcon(String path, String description)
    {
        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }

        URL url = IconManager.class.getResource(path);
        if (url == null)
        {
            LOG.error("Could not load icon. Path not exist: " + path);
        }

        return description == null ? new ImageIcon(url) : new ImageIcon(url, description);
    }


}
