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

package de.fu_berlin.inf.dpp.intellij.ui.views.toolbar;

import de.fu_berlin.inf.dpp.intellij.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.SarosActionFactory;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.UIRefreshListener;
import de.fu_berlin.inf.dpp.intellij.ui.actions.events.SarosActionListener;
import de.fu_berlin.inf.dpp.session.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-29
 * Time: 13:39
 */

public class FollowButton extends ToolbarButton implements SarosActionListener
{
    private JPopupMenu popupMenu;
    private final FollowModeAction followModeAction;

    private UIRefreshListener refreshListener = new UIRefreshListener()
    {
        @Override
        public void refresh(AbstractSarosAction actionEvent)
        {
            createMenu(followModeAction);
        }
    };

    public FollowButton()
    {
        followModeAction = (FollowModeAction) SarosActionFactory.getAction(FollowModeAction.NAME);
        followModeAction.addRefreshListener(refreshListener);

        createButton();
    }

    private void createButton()
    {
        setIcon("followmode", "follow");
        setActionCommand(FollowModeAction.NAME);

        setToolTipText("Enter follow mode");

        createMenu(followModeAction);
        this.setEnabled(false);

        final JButton button = this;
        this.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ev)
            {
                popupMenu.show(button, 0, button.getBounds().y + button.getBounds().height);
            }

        });
    }


    public void createMenu(final FollowModeAction action)
    {
        popupMenu = new JPopupMenu();

        final String prefix = "Follow ";

        boolean isUser = false;
        for (User user : action.getCurrentRemoteSessionUsers())
        {
            isUser = true;
            String userName = user.getHumanReadableName();
            String userNameShort = userName;
            int index = userNameShort.indexOf("@");
            if (index > -1)
            {
                userNameShort = userNameShort.substring(0, index);
            }

            JMenuItem menuItem = new JMenuItem(prefix + userNameShort);

            User currentUser = action.getCurrentlyFollowedUser();

            if (currentUser != null)
            {
                String currentUserName = currentUser.getShortHumanReadableName();
                if (currentUserName.equalsIgnoreCase(userNameShort))
                {
                    menuItem.setEnabled(false);
                }
            }

            menuItem.setActionCommand(userName);
            menuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action.setFollowUser(e.getActionCommand());
                    startAction(action);
                }
            });
            popupMenu.add(menuItem);
        }

        popupMenu.addSeparator();

        JMenuItem leaveItem = new JMenuItem("Leave follow mode");
        leaveItem.setActionCommand(null);
        leaveItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                action.setFollowUser(e.getActionCommand());
                startAction(action);
            }
        });
        leaveItem.setEnabled(isUser);

        popupMenu.add(leaveItem);
    }


    @Override
    public void actionStarted(ISarosAction action)
    {

    }

    @Override
    public void actionFinished(ISarosAction action)
    {

    }
}
