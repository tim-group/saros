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

import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducerAndConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.util.concurrent.CancellationException;

/**
 * This manager is responsible for handling {@link Permission} changes.
 *
 * @author rdjemili
 */
@Component(module = "core")
public class PermissionManager extends AbstractActivityProducerAndConsumer implements Startable
{
    private final ISarosSession sarosSession;

    private static final Logger log = Logger.getLogger(PermissionManager.class);

    public PermissionManager(ISarosSession sarosSession)
    {
        this.sarosSession = sarosSession;
    }

    private final AbstractActivityReceiver receiver = new AbstractActivityReceiver()
    {

        @Override
        public void receive(PermissionActivity activity)
        {
            handlePermissionChange(activity);
        }

    };

    @Override
    public void start()
    {
        sarosSession.addActivityProducerAndConsumer(this);
    }

    @Override
    public void stop()
    {
        sarosSession.removeActivityProducerAndConsumer(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(IActivity activity)
    {
        activity.dispatch(receiver);
    }

    /**
     * This method is responsible for handling incoming permission changes from
     * other clients
     *
     * @param activity
     */
    private void handlePermissionChange(PermissionActivity activity)
    {

        User user = activity.getAffectedUser();
        if (!user.isInSarosSession())
        {
            log.warn("could not change permissions of user " + user
                    + " because the user is longer part of the session");
            return;
        }

        User.Permission permission = activity.getPermission();
        this.sarosSession.setPermission(user, permission);
    }

    /**
     * Initiates a {@link Permission} change for a specific user.
     *
     * @param user          The user who's {@link Permission} has to be changed
     * @param newPermission The new {@link Permission} of the user
     * @param synchronizer  An Abstraction of the SWT-Thread
     * @throws CancellationException
     * @throws InterruptedException
     * @host This method may only called by the host.
     * @noSWT This method mustn't be called from the SWT UI thread
     * @blocking Returning after the {@link Permission} change is complete
     */

    public void initiatePermissionChange(final User user,
            final User.Permission newPermission, UISynchronizer synchronizer)
            throws CancellationException, InterruptedException
    {

        final User localUser = sarosSession.getLocalUser();

        Runnable fireActivityrunnable = new Runnable()
        {

            @Override
            public void run()
            {
                fireActivity(new PermissionActivity(localUser, user,
                        newPermission));

                sarosSession.setPermission(user, newPermission);

            }
        };

        if (user.isHost())
        {
            synchronizer.syncExec(ThreadUtils.wrapSafe(log,
                    fireActivityrunnable));

        }
        else
        {
            StartHandle startHandle = sarosSession.getStopManager().stop(user,
                    "Permission change");

            synchronizer.syncExec(ThreadUtils.wrapSafe(log,
                    fireActivityrunnable));

            if (!startHandle.start())
            {
                log.error("Didn't unblock. "
                        + "There still exist unstarted StartHandles.");
            }
        }
    }
}

