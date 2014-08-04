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

package de.fu_berlin.inf.dpp.core.ui;

import de.fu_berlin.inf.dpp.activities.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.Status;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.runtime.UIMonitoredJob;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The RemoteProgressManager is responsible for showing progress bars on the
 * machines of other users.
 */
public class RemoteProgressManager extends AbstractActivityProducer {

    private static final Logger LOG = Logger
        .getLogger(RemoteProgressManager.class);

    private static final Random RANDOM = new Random();

    private ISarosSession session;

    private final Map<String, RemoteProgress> progressDialogs = new HashMap<String, RemoteProgress>();

    private final ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(User user) {
            for (RemoteProgress progress : progressDialogs.values()) {
                if (progress.isFromUser(user)) {
                    progress.close();
                }
            }
        }
    };

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(ProgressActivity progressActivity) {
            String progressID = progressActivity.getProgressID();
            RemoteProgress progress = progressDialogs.get(progressID);
            if (progress == null) {
                progress = new RemoteProgress(progressID,
                    progressActivity.getSource());
                progressDialogs.put(progressID, progress);
            }
            progress.receive(progressActivity);
        }
    };

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSharedProject) {
            session = newSharedProject;
            session.addActivityConsumer(consumer);
            session.addActivityProducer(RemoteProgressManager.this);
            newSharedProject.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeActivityConsumer(consumer);
            oldSarosSession.removeActivityProducer(RemoteProgressManager.this);
            oldSarosSession.removeListener(sharedProjectListener);
            for (RemoteProgress progress : progressDialogs.values()) {
                progress.close();
            }
            progressDialogs.clear();

            session = null;
        }
    };

    private final SimpleDateFormat format = new SimpleDateFormat("HHmmssSS");

    public RemoteProgressManager(ISarosSessionManager sessionManager) {
        sessionManager.addSarosSessionListener(sessionListener);
    }

    String getNextProgressID() {
        return format.format(new Date()) + RANDOM.nextLong();
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
     */
    public IProgressMonitor createRemoteProgress(
        final ISarosSession sarosSession, final List<User> recipients) {
        return new IProgressMonitor() {

            private final String progressID = getNextProgressID();

            private final User localUser = sarosSession.getLocalUser();

            private int worked = 0;

            private int totalWorked = -1;

            @Override
            public void beginTask(String name, int totalWorked) {
                this.totalWorked = totalWorked;
                createProgressActivityForUsers(localUser, recipients,
                    progressID, 0, totalWorked, name, ProgressAction.UPDATE);
            }

            @Override
            public void done() {
                createProgressActivityForUsers(localUser, recipients,
                    progressID, 0, 0, null, ProgressAction.DONE);
            }

            @Override
            public boolean isCanceled() {
                //TODO: It would be cool to support communicating cancellation
                // to the originator
                return false;
            }

            @Override
            public void setCanceled(boolean value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setTaskName(String name) {
                createProgressActivityForUsers(localUser, recipients,
                    progressID, worked, totalWorked, name,
                    ProgressAction.UPDATE);
            }

            @Override
            public void subTask(String name) {
                createProgressActivityForUsers(localUser, recipients,
                    progressID, worked, totalWorked, name,
                    ProgressAction.UPDATE);
            }

            @Override
            public void worked(int work) {
                worked += work;
                if (worked > totalWorked) {
                    LOG.warn(MessageFormat.format(
                            "Worked ({0})is greater than totalWork ({1}). Forgot to call beginTask?",
                            worked, totalWorked), new StackTrace()
                    );
                }
                createProgressActivityForUsers(localUser, recipients,
                    progressID, worked, totalWorked, null,
                    ProgressAction.UPDATE);
            }

            private void createProgressActivityForUsers(User source,
                List<User> recipients, String progressID, int workCurrent,
                int workTotal, String taskName, ProgressAction action) {
                for (User target : recipients) {
                    fireActivity(
                        new ProgressActivity(source, target, progressID,
                            workCurrent, workTotal, taskName, action)
                    );
                }

            }
        };
    }

    /**
     * This wraps the given progress monitor so that any progress reported via
     * the original monitor is reported to the listed remote hosts too.
     * <p/>
     * Background: Sometimes we run a process locally and need to show the user
     * progress, so he/she can abort the process. But we also need to report the
     * progress to remote users.
     *
     * @param session
     * @param target
     * @param monitor
     * @return
     */
    public IProgressMonitor mirrorLocalProgressMonitorToRemote(
        final ISarosSession session, final User target,
        final IProgressMonitor monitor) {

        return new IProgressMonitor() {
            final String progressID = getNextProgressID();
            final User localUser = RemoteProgressManager.this.session
                .getLocalUser();
            int worked = 0;
            int totalWorked = -1;

            @Override
            public void beginTask(String name, int totalWorked) {
                // update local progress monitor
                monitor.beginTask(name, totalWorked);

                // report to remote monitor!
                this.totalWorked = totalWorked;
                fireActivity(
                    new ProgressActivity(localUser, target, progressID, 0,
                        totalWorked, name, ProgressAction.BEGINTASK)
                );
            }

            @Override
            public void done() {
                monitor.done();
                fireActivity(
                    new ProgressActivity(localUser, target, progressID, 0, 0,
                        null, ProgressAction.DONE)
                );
            }

            @Override
            public boolean isCanceled() {
                return monitor.isCanceled();
            }

            /**
             * FIXME: This is not yet propagated remotely
             */
            @Override
            public void setCanceled(boolean value) {
                fireActivity(
                    new ProgressActivity(localUser, target, progressID, worked,
                        totalWorked, "Cancellation", ProgressAction.CANCEL)
                );
                monitor.setCanceled(value);
            }

            @Override
            public void setTaskName(String name) {
                monitor.setTaskName(name);
                fireActivity(
                    new ProgressActivity(localUser, target, progressID, worked,
                        totalWorked, name, ProgressAction.SETTASKNAME)
                );
            }

            @Override
            public void subTask(String name) {
                monitor.subTask(name);
                fireActivity(
                    new ProgressActivity(localUser, target, progressID, worked,
                        totalWorked, name, ProgressAction.SUBTASK)
                );
            }

            @Override
            public void worked(int work) {
                monitor.worked(work);
                worked += work;
                if (worked > totalWorked) {
                    LOG.warn(MessageFormat.format(
                            "Worked ({0})is greater than totalWork ({1}). Forgot to call beginTask?",
                            worked, totalWorked), new StackTrace()
                    );
                }
                fireActivity(
                    new ProgressActivity(localUser, target, progressID, worked,
                        totalWorked, null, ProgressAction.UPDATE)
                );
            }
        };
    }

    /**
     * A remote progress represents a progress dialog being shown LOCALLY due to
     * ProgressActivities sent to the local user by a remote peer.
     */
    public static class RemoteProgress {

        /**
         * The unique ID of the progress we are showing.
         */
        final String progressID;

        /**
         * The user who requested a progress dialog to be shown.
         */
        final User source;

        /**
         * A queue of incoming ProgressActivities which will be processed
         * locally to update the local Progress dialog.
         */
        final LinkedBlockingQueue<ProgressActivity> activities = new LinkedBlockingQueue<ProgressActivity>();

        public RemoteProgress(String progressID, User source) {
            this.progressID = progressID;
            this.source = source;

            // Run async, so we can continue to receive messages over the
            // network. Run as a job, so that it can be run in background
            // for remote hosts
            UIMonitoredJob job = new UIMonitoredJob(
                "Observing remote progress for " + source.getNickname()) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        mainloop(monitor);
                    } catch (Exception e) {
                        LOG.error("error observing remote progress for "
                            + RemoteProgress.this.source.getNickname(), e);
                        return Status.CANCEL_STATUS;
                    }
                    return Status.OK_STATUS;
                }
            };

            job.schedule();
        }

        public boolean isFromUser(User user) {
            return source.equals(user);
        }

        public synchronized void close() {
            if (activities == null) {
                return;
            }

            /**
             * This Activity is just used as a PoisonPill for the ActivityLoop
             * of the ProgressMonitor (identified by the ProgressAction.DONE)
             * and therefore most values don't have to be set correctly as this
             * Activity will never be sent over the Network.
             */

            receive(new ProgressActivity(source, source, progressID, 0, 0, null,
                ProgressAction.DONE));
        }

        public synchronized void receive(ProgressActivity progressActivity) {
            if (!source.equals(progressActivity.getSource())) {
                LOG.warn("RemoteProgress with ID: " + progressID
                    + " is owned by user " + source
                    + " rejecting packet from other user: " + progressActivity);
                return;
            }
            if (activities == null) {
                LOG.debug("RemoteProgress with ID: " + progressID
                    + " has already been closed. Discarding activity: "
                    + progressActivity);
                return;
            }
            activities.add(progressActivity);
        }

        void mainloop(IProgressMonitor subMonitor) {
            int worked = 0;
            boolean firstTime = true;

            while (true) {
                ProgressActivity activity;
                try {
                    if (subMonitor.isCanceled()) {
                        return;
                    }
                    activity = activities.poll(1000, TimeUnit.MILLISECONDS);
                    if (activity == null) {
                        continue;
                    }
                } catch (InterruptedException e) {
                    return;
                }
                String taskName = activity.getTaskName();
                int newWorked;
                LOG.debug(
                    "RemoteProgressActivity: " + taskName + " / " + activity
                        .getAction()
                );

                switch (activity.getAction()) {
                case BEGINTASK:
                    subMonitor.beginTask(taskName, activity.getWorkTotal());
                    break;
                case SETTASKNAME:
                    subMonitor.setTaskName(taskName);
                    break;
                case SUBTASK:
                    if (taskName != null) {
                        subMonitor.subTask(taskName);
                    }
                    newWorked = activity.getWorkCurrent();
                    if (newWorked > worked) {
                        subMonitor.worked(newWorked - worked);
                        worked = newWorked;
                    }
                    break;
                case UPDATE:
                    if (firstTime) {
                        subMonitor.beginTask(taskName, activity.getWorkTotal());
                        firstTime = false;
                    } else {
                        if (taskName != null) {
                            subMonitor.subTask(taskName);
                        }

                        newWorked = activity.getWorkCurrent();
                        if (newWorked > worked) {
                            subMonitor.worked(newWorked - worked);
                            worked = newWorked;
                        }
                    }
                    break;
                case DONE:
                    subMonitor.done();
                    return;
                case CANCEL:
                    LOG.info("Progress was cancelled by remote user");
                    subMonitor.setCanceled(true);
                    return;
                }
            }
        }
    }
}
