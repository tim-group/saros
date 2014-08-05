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
import de.fu_berlin.inf.dpp.activities.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.Status;
import de.fu_berlin.inf.dpp.intellij.runtime.UIMonitoredJob;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A remote progress represents a progress dialog being shown locally which is
 * updated via {@link de.fu_berlin.inf.dpp.activities.ProgressActivity activities} sent by a remote user.
 */
final class RemoteProgress {

    private static final Logger LOG = Logger.getLogger(RemoteProgress.class);

    /**
     * The unique ID of this progress.
     */
    private final String id;

    /**
     * The user who requested a progress dialog to be shown.
     */
    private final User source;

    private final RemoteProgressManager rpm;

    private boolean running;

    private boolean started;

    /**
     * A queue of incoming ProgressActivities which will be processed locally to
     * update the local Progress dialog.
     */
    private LinkedBlockingQueue<ProgressActivity> activities = new LinkedBlockingQueue<ProgressActivity>();

    RemoteProgress(final RemoteProgressManager rpm, final String id,
        final User source) {
        this.rpm = rpm;
        this.id = id;
        this.source = source;
    }

    User getSource() {
        return source;
    }

    synchronized void start() {
        if (started)
            return;

        started = true;

        final UIMonitoredJob job = new UIMonitoredJob(
            "Observing remote progress for " + source.getNickname()) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    mainloop(monitor);
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    LOG.error(e);
                    return Status.CANCEL_STATUS;
                } finally {
                    rpm.removeProgress(id);
                }
            }
        };
        job.schedule();
        running = true;
    }

    synchronized void close() {
        if (!running)
            return;

        running = true;

        /**
         * This Activity is just used as a PoisonPill for the ActivityLoop of
         * the ProgressMonitor (identified by the ProgressAction.DONE) and
         * therefore most values don't have to be set correctly as this Activity
         * will never be sent over the Network.
         */

        execute(new ProgressActivity(source, source, id, 0, 0, null,
            ProgressAction.DONE));
    }

    synchronized void execute(ProgressActivity activity) {
        if (!source.equals(activity.getSource())) {
            LOG.warn(
                "RemoteProgress with ID: " + id + " is owned by user " + source
                    + " rejecting activity from other user: " + activity);
            return;
        }

        if (!running) {
            LOG.debug("RemoteProgress with ID: " + id
                + " has already been closed. Discarding activity: " + activity);
            return;
        }

        activities.add(activity);
    }

    private void mainloop(final IProgressMonitor monitor) {
        int worked = 0;
        boolean firstTime = true;

        update:
        while (true) {

            final ProgressActivity activity;

            try {
                if (monitor.isCanceled())
                    break update;

                // poll so this monitor can be closed locally
                activity = activities.poll(1000, TimeUnit.MILLISECONDS);

                if (activity == null)
                    continue update;

            } catch (InterruptedException e) {
                return;
            }

            final String taskName = activity.getTaskName();

            int newWorked;

            if (LOG.isTraceEnabled())
                LOG.trace("executing progress activity: " + activity);

            switch (activity.getAction()) {
            case BEGINTASK:
                monitor.beginTask(taskName, activity.getWorkTotal());
                continue update;
            case SETTASKNAME:
                monitor.setTaskName(taskName);
                continue update;
            case SUBTASK:
                if (taskName != null)
                    monitor.subTask(taskName);
                newWorked = activity.getWorkCurrent();
                if (newWorked > worked) {
                    monitor.worked(newWorked - worked);
                    worked = newWorked;
                }
                continue update;
            case UPDATE:
                if (firstTime) {
                    monitor.beginTask(taskName, activity.getWorkTotal());
                    firstTime = false;
                } else {
                    if (taskName != null)
                        monitor.subTask(taskName);

                    newWorked = activity.getWorkCurrent();
                    if (newWorked > worked) {
                        monitor.worked(newWorked - worked);
                        worked = newWorked;
                    }
                }
                continue update;
            case DONE:
                monitor.done();
                break update;
            case CANCEL:
                LOG.debug("progress was cancelled by remote user");
                monitor.setCanceled(true);
                break update;
            }
        }
    }

    @Override
    public int hashCode() {
        return ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof RemoteProgress))
            return false;

        return ObjectUtils.equals(id, ((RemoteProgress) obj).id) && ObjectUtils
            .equals(source, ((RemoteProgress) obj).source);
    }
}
