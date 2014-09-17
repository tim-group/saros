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

package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import de.fu_berlin.inf.dpp.core.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.core.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.picocontainer.annotations.Inject;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Button to follow a user. Displays a PopupMenu containing all session users to choose
 * from.
 */
public class FollowButton extends ToolbarButton
{
    public static final String FOLLOW_ICON_PATH = "icons/ovr16/followmode.png";
    private JPopupMenu popupMenu;
    private final FollowModeAction followModeAction;

    private final ISharedProjectListener userListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(final User user) {
            updateMenu();
        }

        @Override
        public void userJoined(final User user) {
            updateMenu();
        }
    };

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {
            session.addListener(userListener);
            updateMenu();
            setEnabled(true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(userListener);
            updateMenu();
            setEnabled(false);
        }
    };

    private final ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(final User user,
            final boolean isFollowed) {
            updateMenu();
        }
    };

    private String menuItemPrefix;

    @Inject
    public ISarosSessionManager sessionManager;

    @Inject
    public EditorManager editorManager;

    private ISarosSession session;


    public FollowButton()
    {
        super(FollowModeAction.NAME, "Follow", FOLLOW_ICON_PATH, "Enter follow mode");

        followModeAction = new FollowModeAction();

        sessionManager.addSarosSessionListener(sessionListener);

        editorManager.addSharedEditorListener(editorListener);

        createMenu();
        setEnabled(false);

        final JButton button = this;
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                popupMenu.show(button, 0,
                    button.getBounds().y + button.getBounds().height);
            }

        });
    }

    public void createMenu()
    {
        popupMenu = new JPopupMenu();

        menuItemPrefix = "Follow ";

        for (User user : followModeAction.getCurrentRemoteSessionUsers())
        {
            JMenuItem menuItem = createItemForUser(user);
            popupMenu.add(menuItem);
        }

        popupMenu.addSeparator();

        JMenuItem leaveItem = new JMenuItem("Leave follow mode");
        leaveItem.setActionCommand(null);
        leaveItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                followModeAction.execute(e.getActionCommand());
            }
        });
        leaveItem.setEnabled(followModeAction.getCurrentlyFollowedUser() != null);

        popupMenu.add(leaveItem);
    }

    private JMenuItem createItemForUser(User user) {
        String userName = user.getNickname();
        String userNameShort = userName;
        int index = userNameShort.indexOf("@");
        if (index > -1)
        {
            userNameShort = userNameShort.substring(0, index);
        }

        JMenuItem menuItem = new JMenuItem(menuItemPrefix + userNameShort);

        User currentlyFollowedUser = followModeAction.getCurrentlyFollowedUser();
        if (currentlyFollowedUser != null)
        {
            String currentUserName = currentlyFollowedUser.getNickname();
            if (currentUserName.equalsIgnoreCase(userNameShort))
            {
                menuItem.setEnabled(false);
            }
        }

        menuItem.setActionCommand(userName);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                followModeAction.execute(e.getActionCommand());
            }
        });
        return menuItem;
    }

    private void updateMenu() {
        ThreadUtils.runSafeAsync(LOG, new Runnable() {
            @Override
            public void run() {
                createMenu();
            }
        });
    }
}
