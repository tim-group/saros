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

import de.fu_berlin.inf.dpp.activities.QueueItem;
import de.fu_berlin.inf.dpp.activities.business.*;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.management.TransformationResult;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This handler is responsible for handling the correct thread access when
 * transforming activities with the {@link ConcurrentDocumentServer} and
 * {@link ConcurrentDocumentClient}. The sending and executing of activities
 * <b>must</b> be done in {@linkplain IActivityHandlerCallback callback} as it
 * is <b>not</b> performed by this handler !
 *
 * @author Stefan Rossbach
 */
public final class ActivityHandler implements Startable
{

    private static final Logger LOG = Logger.getLogger(ActivityHandler.class);

    /**
     * join timeout when stopping this component
     */
    private static final long TIMEOUT = 10000;

    private static final int DISPATCH_MODE_SYNC = 0;

    private static final int DISPATCH_MODE_ASYNC = 1; // Experimental

    private static final int DISPATCH_MODE;

    static
    {
        int dispatchModeToUse = Integer.getInteger(
                "de.fu_berlin.inf.dpp.session.ACTIVITY_DISPATCH_MODE",
                DISPATCH_MODE_SYNC);

        if (dispatchModeToUse != DISPATCH_MODE_ASYNC)
        {
            dispatchModeToUse = DISPATCH_MODE_SYNC;
        }

        DISPATCH_MODE = dispatchModeToUse;
    }

    private final LinkedBlockingQueue<List<IActivity>> dispatchQueue = new LinkedBlockingQueue<List<IActivity>>();

    private final IActivityHandlerCallback callback;

    private final ISarosSession session;

    private final ConcurrentDocumentServer documentServer;

    private final ConcurrentDocumentClient documentClient;

    private final UISynchronizer synchronizer;

    /*
     * We must use a thread for synchronous execution otherwise we would block
     * the DispatchThreadContext which handles the dispatching of all network
     * packets
     */
    private Thread dispatchThread;

    private final Runnable dispatchThreadRunnable = new Runnable()
    {

        @Override
        public void run()
        {
            LOG.debug("activity dispatcher started");
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    dispatchAndExecuteActivities(dispatchQueue.take());
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
            LOG.debug("activity dispatcher stopped");
        }
    };

    public ActivityHandler(ISarosSession session,
            IActivityHandlerCallback callback,
            ConcurrentDocumentServer documentServer,
            ConcurrentDocumentClient documentClient, UISynchronizer synchronizer)
    {
        this.session = session;
        this.callback = callback;
        this.documentServer = documentServer;
        this.documentClient = documentClient;
        this.synchronizer = synchronizer;
    }

    /**
     * Transforms and dispatches the activities. The
     * {@linkplain IActivityHandlerCallback callback} will be notified about the
     * results.
     *
     * @param activities an <b>immutable</b> list containing the activities
     */

    public synchronized void handleIncomingActivities(List<IActivity> activities)
    {
        System.out.println("ActivityHandler.handleIncomingActivities>>>>>>>>>>"+activities);


        if (session.isHost())
        {

            /**
             * @JTourBusStop 8, Activity sending, Activity Server:
             *
             *               This is where the server receives activities. The
             *               Server may transform activities again if necessary
             *               and afterward sends them to the correct clients.
             */

            TransformationResult result = directServerActivities(activities);
            activities = result.getLocalActivities();
            for (QueueItem item : result.getSendToPeers())
            {

                List<User> recipients = getRecipientsForQueueItem(item);
                callback.send(recipients, item.activity);
            }
        }

        /**
         * @JTourBusStop 9, Activity sending, Client Receiver:
         *
         *               This is the part where clients will receive activities.
         *               These activities are put into the queue of the activity
         *               dispatcher. This queue is consumed by the
         *               dispatchThread which transforms activities again if
         *               necessary and then forwards it to the SarosSession.
         *
         */

        if (activities.isEmpty())
        {
            return;
        }

        if (DISPATCH_MODE == DISPATCH_MODE_ASYNC)
        {
            dispatchAndExecuteActivities(activities);
        }
        else
        {
            dispatchQueue.add(activities);
        }
    }

    /**
     * Determines the recipients for a given QueueItem
     *
     * @param item the QueueItem for which the participants should be determined
     * @return a list of participants this activity should be sent to
     */
    private List<User> getRecipientsForQueueItem(QueueItem item)
    {

        /*
         * If the Activity is a IResourceActivity check that the user can
         * actually process them
         */
        List<User> recipients = new ArrayList<User>();
        if (item.activity instanceof IResourceActivity)
        {
            IResourceActivity activity = (IResourceActivity) item.activity;
            /*
             * HACK: IRessourceActivities with null as path will be treated as
             * not being resource related as we can't decide whether to send
             * them or not. IRessourceActivities must not have null as path but
             * as the EditorActivity currently break this and uses null paths
             * for non-shared-files we have to make this distinction for now.
             */
            if (activity.getPath() == null)
            {
                recipients = item.recipients;
            }
            else
            {
                for (User user : item.recipients)
                {
                    if (session.userHasProject(user, activity.getPath()
                            .getProject()))
                    {
                        recipients.add(user);
                    }
                }
            }
        }
        else
        {
            recipients = item.recipients;
        }
        return recipients;
    }

    /**
     * @JTourBusStop 6, Activity sending, Transforming the IActivity (Client):
     *
     *               This function will transform activities and then forward
     *               them to the callback. E.g. this will turn TextEditActivity
     *               into Jupiter activities.
     *
     *               Saros uses a client-server-architecture. All activities
     *               will first be send to the server located at the Host. The
     *               Host himself also acts as a client, but houses an
     *               additional server-part.
     */

    /**
     * Transforms and determines the recipients of the activities. The
     * {@linkplain IActivityHandlerCallback callback} will be notified about the
     * results.
     *
     * @param activities an <b>immutable</b> list containing the activities
     */
    /*
     * Note: transformation and executing has to be performed together in the
     * SWT thread. Else, it would be possible that local activities are executed
     * between transformation and application of remote operations. In other
     * words, the transformation would be applied to an out-dated state.
     */
    public void handleOutgoingActivities(final List<IActivity> activities)
    {
        synchronizer.syncExec(ThreadUtils.wrapSafe(LOG, new Runnable()
        {

            @Override
            public void run()
            {
                for (IActivity activity : activities)
                {

                    IActivity transformationResult = documentClient
                            .transformToJupiter(activity);

                    callback.send(Collections.singletonList(session.getHost()),
                            transformationResult);

                }
            }
        }));
    }

    @Override
    public void start()
    {
        if (DISPATCH_MODE == DISPATCH_MODE_ASYNC)
        {
            return;
        }

        dispatchThread = ThreadUtils.runSafeAsync("ActivityDispatcher", LOG,
                dispatchThreadRunnable);
    }

    @Override
    public void stop()
    {
        if (DISPATCH_MODE == DISPATCH_MODE_ASYNC)
        {
            return;
        }

        dispatchThread.interrupt();
        try
        {
            dispatchThread.join(TIMEOUT);
        }
        catch (InterruptedException e)
        {
            LOG.warn("interrupted while waiting for "
                    + dispatchThread.getName() + " thread to terminate");

            Thread.currentThread().interrupt();
        }

        if (dispatchThread.isAlive())
        {
            LOG.error(dispatchThread.getName() + " thread is still running");
        }
    }

    /**
     * Executes the current activities by dispatching the received activities to
     * the SWT EDT.
     * <p/>
     * We must use synchronous dispatching as it is possible that some handlers
     * or Eclipse itself open dialogs during the execution of an activity.
     * <p/>
     * If the current activity list would be dispatched asynchronously it is
     * possible that further activities may be executed during the currently
     * executed activity list and so leading up to unknown errors.
     * <p/>
     * <pre>
     * Activities to execute:
     * [A, B, C, D, E, F, G, H]
     *           ^
     *           |
     *           --> is currently blocked by a dialog
     *
     * new activities arrive
     * [J, K, L, M]
     *
     * final execution order can be:
     *
     * [A, B, C, D_B, J, K, L, M, D_A, E, F, G, H]
     *
     * Where D_B(efore) is the code that has been executed before
     * entering the modal context and D_A(fter) the code after
     * leaving the modal context.
     *
     * Note: If the next activities list also contains an activity that
     * uses a modal context the execution chain will become even less
     * predictable !
     * </pre>
     *
     * @param activities the activities to execute
     * @see ModalContext
     * @see Window#setBlockOnOpen(boolean shouldBlock)
     * @see IRunnableContext#run(boolean fork, boolean cancelable,
     *      IRunnableWithProgress runnable)
     */
    /*
     * Note: transformation and executing has to be performed together in the
     * SWT thread. Else, it would be possible that local activities are executed
     * between transformation and application of remote operations. In other
     * words, the transformation would be applied to an out-dated state.
     */
    private void dispatchAndExecuteActivities(final List<IActivity> activities)
    {
        Runnable transformingRunnable = new Runnable()
        {
            @Override
            public void run()
            {

                for (IActivity activity : activities)
                {

                    User source = activity.getSource();

                    /*
                     * Ensure that we do not execute activities after all
                     * listeners were notified (See SarosSession#removeUser). It
                     * is still possible that a user may left during activity
                     * execution but this is likely no to produce any errors.
                     *
                     * TODO: as the notification for users who left the session
                     * is send in parallel with the activities there will be
                     * race conditions were one user may execute a given
                     * activity but another user will not which may lead to
                     * unwanted inconsistencies if that activity was a resource
                     * activity.
                     */
                    if (source == null || !source.isInSarosSession())
                    {
                        LOG.warn("dropping activity for user that is no longer in session: "
                                + activity);
                        continue;
                    }

                    List<IActivity> transformedActivities = documentClient
                            .transformFromJupiter(activity);

                    for (IActivity transformedActivity : transformedActivities)
                    {
                        try
                        {
                            callback.execute(transformedActivity);
                        }
                        catch (Exception e)
                        {
                            LOG.error(
                                    "failed to execute activity: " + activity, e);
                        }
                    }
                }

            }
        };

        if (LOG.isTraceEnabled())
        {
            LOG.trace("dispatching " + activities.size()
                    + " activities [mode = " + DISPATCH_MODE + "] : " + activities);
        }

        if (DISPATCH_MODE == DISPATCH_MODE_SYNC)
        {
            synchronizer.syncExec(ThreadUtils.wrapSafe(LOG,
                    transformingRunnable));
        }
        else
        {
            synchronizer.asyncExec(ThreadUtils.wrapSafe(LOG,
                    transformingRunnable));
        }
    }

    /**
     * This method is responsible for directing activities received at the
     * server to the various clients.
     *
     * @param activities A list of incoming activities
     * @return A number of targeted activities.
     */
    private TransformationResult directServerActivities(
            List<IActivity> activities)
    {

        TransformationResult result = new TransformationResult(
                session.getLocalUser());

        final List<User> remoteUsers = session.getRemoteUsers();
        final List<User> allUsers = session.getUsers();

        for (IActivity activity : activities)
        {

            if (activity instanceof FileActivity)
            {
                documentServer.checkFileDeleted(activity);
            }

            if (activity instanceof JupiterActivity
                    || activity instanceof ChecksumActivity)
            {

                result.addAll(documentServer.transformIncoming(activity));
            }
            else if (activity instanceof ITargetedActivity)
            {
                ITargetedActivity target = (ITargetedActivity) activity;
                result.add(new QueueItem(target.getTarget(), activity));

            }
            else if (remoteUsers.size() > 0)
            {

                // We must not send the activity back to the sender
                List<User> receivers = new ArrayList<User>();
                for (User user : allUsers)
                {
                    if (!user.equals(activity.getSource()))
                    {
                        receivers.add(user);
                    }
                }
                result.add(new QueueItem(receivers, activity));

                /*
                 * should we really execute an activity from a user that is
                 * about to or has left the session ?
                 */
            }
            else if (!(session.getLocalUser().equals(activity.getSource())))
            {
                result.executeLocally.add(activity);
            }
        }
        return result;
    }
}
