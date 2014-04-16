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

package de.fu_berlin.inf.dpp.core.project.internal;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.inf.dpp.core.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.business.StopFollowingActivity;
import de.fu_berlin.inf.dpp.annotations.Component;

import de.fu_berlin.inf.dpp.session.AbstractActivityProducerAndConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This manager is responsible for distributing knowledge about changes in
 * follow modes between session participants
 *
 * @author Alexander Waldmann (contact@net-corps.de)
 */
@Component(module = "core")
public class FollowingActivitiesManager extends AbstractActivityProducerAndConsumer {

    private static final Logger log = Logger
            .getLogger(FollowingActivitiesManager.class);

    protected final List<IFollowModeChangesListener> internalListeners = new LinkedList<IFollowModeChangesListener>();
    protected ISarosSession sarosSession;
    protected AwarenessInformationCollector awarenessInformationCollector;

    public FollowingActivitiesManager(ISarosSessionManager sessionManager,
            EditorManager editorManager,
            AwarenessInformationCollector awarenessInformationCollector) {
        this.awarenessInformationCollector = awarenessInformationCollector;
        sessionManager.addSarosSessionListener(sessionListener);
        editorManager
                .addSharedEditorListener(new AbstractSharedEditorListener() {
                    @Override
                    public void followModeChanged(User followedUser,
                            boolean isFollowed) {
                        if (sarosSession == null) {
                            log.error("FollowModeChanged Event listener got a call without a running session.");
                            return;
                        }

                        if (isFollowed) {
                            fireActivity(new StartFollowingActivity(sarosSession
                                    .getLocalUser(), followedUser));
                        } else {
                            fireActivity(new StopFollowingActivity(sarosSession
                                    .getLocalUser()));

                        }
                    }
                });
    }

    @Override
    public void exec(IActivity activity) {
        activity.dispatch(receiver);
    }

    protected AbstractActivityReceiver receiver = new AbstractActivityReceiver() {
        @Override
        public void receive(StartFollowingActivity activity) {
            User user = activity.getSource();
            if (!user.isInSarosSession()) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "illegal follow mode activity received", user));
            }

            log.info("Received new follow mode from: "
                    + user.getHumanReadableName() + " followed User: "
                    + activity.getFollowedUser().getHumanReadableName());

            awarenessInformationCollector.setUserFollowing(user,
                    activity.getFollowedUser());
            notifyListeners();
        }

        @Override
        public void receive(StopFollowingActivity activity) {
            User user = activity.getSource();
            if (!user.isInSarosSession()) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "illegal follow mode activity received", user));
            }

            log.info("User " + user.getHumanReadableName()
                    + " stopped follow mode");

            awarenessInformationCollector.setUserFollowing(user, null);
            notifyListeners();
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession session) {
            sarosSession = session;
            awarenessInformationCollector.flushFollowModes();
            session.addActivityProducerAndConsumer(FollowingActivitiesManager.this);
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            awarenessInformationCollector.flushFollowModes();
            session.removeActivityProducerAndConsumer(FollowingActivitiesManager.this);
            sarosSession = null;
        }
    };

    public void notifyListeners() {
        for (IFollowModeChangesListener listener : this.internalListeners) {
            listener.followModeChanged();
        }
    }

    public void addIinternalListener(IFollowModeChangesListener listener) {
        this.internalListeners.add(listener);
    }

    public void removeIinternalListener(IFollowModeChangesListener listener) {
        this.internalListeners.remove(listener);
    }

}
