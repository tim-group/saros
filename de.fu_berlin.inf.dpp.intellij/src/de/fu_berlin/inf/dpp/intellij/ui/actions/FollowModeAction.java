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

import de.fu_berlin.inf.dpp.core.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.core.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.picocontainer.annotations.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Action to activate follow mode.
 */
public class FollowModeAction extends AbstractSarosAction {

    public static final String NAME = "follow";

    private final ISharedProjectListener userListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(final User user) {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    refreshAll();
                }
            });
        }

        @Override
        public void userJoined(final User user) {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    refreshAll();

                }
            });
        }
    };

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {

            session.addListener(userListener);

            ThreadUtils.runSafeAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    FollowModeAction.this.session = session;
                    refreshAll();
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(userListener);
            ThreadUtils.runSafeAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    session = null;
                    refreshAll();
                }
            });
        }
    };

    private final ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(final User user,
            final boolean isFollowed) {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    refreshAll();
                }
            });
        }
    };

    @Inject
    public ISarosSessionManager sessionManager;

    @Inject
    public EditorManager editorManager;

    private ISarosSession session;

    public FollowModeAction(EditorManager editorManager,
        ISarosSessionManager sessionManager) {
        this.editorManager = editorManager;
        this.sessionManager = sessionManager;

        sessionManager.addSarosSessionListener(sessionListener);

        editorManager.addSharedEditorListener(editorListener);

        if (session != null) {
            session.addListener(userListener);
        }

        refreshAll();
    }

    @Override
    public String getActionName() {
        return NAME;
    }

    public void execute(String userName) {
        if (session == null) {
            return;
        }

        editorManager.setFollowing(findUser(userName));

        actionFinished();
    }

    @Override
    public void execute() {
        //never called
    }

    public User getCurrentlyFollowedUser() {
        return editorManager.getFollowedUser();
    }

    public List<User> getCurrentRemoteSessionUsers() {
        if (session == null)
            return new ArrayList<User>();

        return session.getRemoteUsers();

    }

    private User findUser(String userName) {
        if (userName == null) {
            return null;
        }

        for (User user : getCurrentRemoteSessionUsers()) {
            String myUserName = user.getNickname();
            if (myUserName.equalsIgnoreCase(userName)) {
                return user;
            }
        }

        return null;
    }
}
