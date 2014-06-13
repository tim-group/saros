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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import org.picocontainer.annotations.Inject;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Follows active session
 * <p/>
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class FollowModeAction extends AbstractSarosAction
{

    public static final String NAME = "follow";


    private ISharedProjectListener userListener = new AbstractSharedProjectListener()
    {
        @Override
        public void userLeft(final User user)
        {
            SWTUtils.runSafeSWTAsync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    currentRemoteSessionUsers.remove(user);

                    if (user.equals(currentlyFollowedUser))
                    {
                        currentlyFollowedUser = null;
                        updateMenu();
                    }
                }
            });
        }

        @Override
        public void userJoined(final User user)
        {
            SWTUtils.runSafeSWTAsync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    currentRemoteSessionUsers.add(user);
                    updateMenu();

                }
            });
        }
    };

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener()
    {
        @Override
        public void sessionStarted(final ISarosSession session)
        {

            session.addListener(userListener);

            SWTUtils.runSafeSWTAsync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    FollowModeAction.this.session = session;
                    currentRemoteSessionUsers.clear();
                    currentRemoteSessionUsers.addAll(session.getRemoteUsers());
                    updateMenu();
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession)
        {
            oldSarosSession.removeListener(userListener);
            SWTUtils.runSafeSWTAsync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    FollowModeAction.this.session = null;
                    currentRemoteSessionUsers.clear();
                    updateMenu();
                }
            });
        }
    };

    private ISharedEditorListener editorListener = new AbstractSharedEditorListener()
    {
        @Override
        public void followModeChanged(final User user, final boolean isFollowed)
        {
            SWTUtils.runSafeSWTAsync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    currentlyFollowedUser = user;

                    if (!isFollowed)
                    {
                        currentlyFollowedUser = null;
                    }

                    updateMenu();
                }
            });
        }
    };

    @Inject
    public ISarosSessionManager sessionManager;

    @Inject
    public EditorManager editorManager;

    private ISarosSession session;

    private User currentlyFollowedUser;

    private final Set<User> currentRemoteSessionUsers = new LinkedHashSet<User>();


    public FollowModeAction(EditorManager editorManager, ISarosSessionManager sessionManager)
    {
        this.editorManager = editorManager;
        this.sessionManager = sessionManager;

        sessionManager.addSarosSessionListener(sessionListener);

        editorManager.addSharedEditorListener(editorListener);
        currentlyFollowedUser = editorManager.getFollowedUser();

        if (session != null)
        {
            session.addListener(userListener);
            currentRemoteSessionUsers.addAll(session.getRemoteUsers());
        }

        updateMenu();
    }

    /**
     *
     */
    private FollowModeAction()
    {
        session = sessionManager.getSarosSession();

        sessionManager.addSarosSessionListener(sessionListener);
        editorManager.addSharedEditorListener(editorListener);
        currentlyFollowedUser = editorManager.getFollowedUser();

        if (session != null)
        {
            currentRemoteSessionUsers.addAll(session.getRemoteUsers());
        }

        updateMenu();
    }


    @Override
    public String getActionName()
    {
        return NAME;

    }

    @Override
    public void run()
    {
        actionStarted();


        if (session == null)
        {
            return;
        }

        User newFollowUser = findUser(followUserName);
        followUser(newFollowUser);

        actionFinished();
    }

    private void followUser(User user)
    {
        currentlyFollowedUser = user;
        editorManager.setFollowing(currentlyFollowedUser);
    }

    private String followUserName;

    public void setFollowUser(String followUserName)
    {
        this.followUserName = followUserName;
    }

    public User getCurrentlyFollowedUser()
    {
        return editorManager.getFollowedUser();
    }

    public Set<User> getCurrentRemoteSessionUsers()
    {
        return currentRemoteSessionUsers;
    }

    private User findUser(String userName)
    {
        if (userName == null)
        {
            return null;
        }


        for (User user : getCurrentRemoteSessionUsers())
        {
            String myUserName = user.getNickname();
            if (myUserName.equalsIgnoreCase(userName))
            {
                return user;
            }
        }

        return null;
    }


    /**
     * Returns the next user to follow or <code>null</code> if follow mode
     * should be disabled.
     */
    private User getNextUserToFollow()
    {
        if (currentRemoteSessionUsers.isEmpty())
        {
            return null;
        }

        if (currentlyFollowedUser == null)
        {
            return currentRemoteSessionUsers.iterator().next();
        }

        User nextUser = null;

        for (Iterator<User> it = currentRemoteSessionUsers.iterator(); it
                .hasNext(); )
        {
            User user = it.next();
            if (user.equals(currentlyFollowedUser))
            {
                if (it.hasNext())
                {
                    nextUser = it.next();
                }

                break;
            }
        }

        return nextUser;
    }


    private void updateMenu()
    {
        refreshAll();
    }

}
