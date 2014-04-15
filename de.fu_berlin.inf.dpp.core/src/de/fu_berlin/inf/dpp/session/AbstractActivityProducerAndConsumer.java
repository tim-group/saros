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

package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractActivityProducerAndConsumer implements
        IActivityProducerAndConsumer {

    protected List<IActivityListener> activityListeners = new CopyOnWriteArrayList<IActivityListener>();

    @Override
    public void addActivityListener(IActivityListener listener) {
        assert listener != null;
        if (!activityListeners.contains(listener)) {
            this.activityListeners.add(listener);
        }
    }

    @Override
    public abstract void exec(IActivity activity);

    @Override
    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    /**
     * @JTourBusStop 2, Activity sending, The abstract class to extend:
     *
     *               But instead of implementing the
     *               IActivityProducerAndConsumer interface one should extend
     *               the AbstractActivityProducerAndConsumer class and call the
     *               fireActivity method on newly created activities to inform
     *               all listeners.
     */
    public void fireActivity(IActivity activity) {
        for (IActivityListener activityListener : activityListeners) {
            activityListener.activityCreated(activity);
        }
    }
}
