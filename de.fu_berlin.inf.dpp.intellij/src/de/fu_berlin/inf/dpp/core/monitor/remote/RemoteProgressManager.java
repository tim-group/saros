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

package de.fu_berlin.inf.dpp.core.monitor.remote;

import de.fu_berlin.inf.dpp.activities.ProgressActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The RemoteProgressManager is responsible for showing progress bars on the
 * machines of other users.
 */
@Component(module = "core")
// FIXME this component has NO flow control, it can flood the network layer
public class RemoteProgressManager extends AbstractActivityProducer {

    private static final Random RANDOM = new Random();

    //TODO: Uncomment when ISarosSessionManager was added
    //private final ISarosSessionManager sessionManager;

    private volatile ISarosSession session;

    // the id should be unique enough
    // closing a progress will remove itself from the map
    private final Map<String, RemoteProgress> progresses = Collections
        .synchronizedMap(new HashMap<String, RemoteProgress>());

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(ProgressActivity progressActivity) {

            RemoteProgress progress;
            final String id = progressActivity.getProgressID();

            synchronized (progresses) {
                progress = progresses.get(id);

                if (progress == null) {
                    progress = new RemoteProgress(RemoteProgressManager.this,
                        id, progressActivity.getSource());

                    progresses.put(id, progress);

                    progress.start();
                }
            }

            progress.execute(progressActivity);
        }
    };

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(User user) {

            final List<RemoteProgress> progressesToClose = new ArrayList<RemoteProgress>();

            synchronized (progresses) {
                for (final RemoteProgress progress : progresses.values()) {
                    if (progress.getSource().equals(user))
                        progressesToClose.add(progress);
                }
            }

            for (final RemoteProgress progress : progressesToClose)
                progress.close();
        }
    };

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            RemoteProgressManager.this.session = session;
            session.addActivityConsumer(consumer);
            session.addActivityProducer(RemoteProgressManager.this);
            session.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            session.removeActivityConsumer(consumer);
            session.removeActivityProducer(RemoteProgressManager.this);
            session.removeListener(sharedProjectListener);

            final List<RemoteProgress> progressesToClose = new ArrayList<RemoteProgress>();

            synchronized (progresses) {
                progressesToClose.addAll(progresses.values());
            }

            for (final RemoteProgress progress : progressesToClose)
                progress.close();

            RemoteProgressManager.this.session = null;
        }
    };

    //TODO: Uncomment when ISarosSessionManager was added
    public RemoteProgressManager(/*ISarosSessionManager sessionManager*/) {
        /*this.sessionManager = sessionManager;
        this.sessionManager.addSarosSessionListener(sessionListener);*/
    }

    /**
     * Returns a new IProgressMonitor which is displayed at the given remote
     * sites.
     * <p/>
     * Usage:
     * <p/>
     * - Call beginTask with the name of the Task to show to the user and the
     * total amount of work.
     * <p/>
     * - Call worked to add amounts of work your task has finished (this will be
     * summed up and should not exceed totalWorked
     * <p/>
     * - Call done as a last method to close the progress on the remote side.
     * <p/>
     * Caution: This class does not check many invariants, but rather only sends
     * your commands to the remote party.
     *
     * @param users
     * @param monitor
     * @return
     */
    public IProgressMonitor createRemoteProgress(final List<User> users,
        final IProgressMonitor monitor) {

        ISarosSession currentSession = session;

        if (currentSession == null)
            return monitor == null ? new NullProgressMonitor() : monitor;

        return new RemoteProgressMonitor(this, getNextID(),
            currentSession.getLocalUser(),
            new ArrayList<User>(new HashSet<User>(users)), monitor
        );
    }

    void monitorUpdated(final ProgressActivity activity) {
        fireActivity(activity);
    }

    /**
     * Removes a {@linkplain RemoteProgress progress}. The progress will no
     * longer receive any updates.
     *
     * @param id the id of the progress
     */
    void removeProgress(final String id) {
        progresses.remove(id);
    }

    private String getNextID() {
        return Long.toHexString(RANDOM.nextLong());
    }
}
