package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * An {@link IActivityProducer} is expected to listen for certain events or
 * actions, e.g. in the IDE, create new {@link IActivity} objects, and inform
 * all registered {@link IActivityListener}s about this. The action represented
 * by such an {@link IActivity} needs to be performed locally first, and then an
 * {@link IActivity} is created and given to the {@link IActivityListener}s.
 * <p>
 * Instead of implementing this interface from scratch, you probably want to
 * subclass {@link AbstractActivityProducer} instead.
 */
public interface IActivityProducer {
    /**
     * Registers the given listener, so it will be informed via
     * {@link IActivityListener#created(IActivity)}.
     */
    public void addActivityListener(IActivityListener listener);

    /**
     * Removes a listener previously registered with
     * {@link #addActivityListener(IActivityListener)}.
     */
    public void removeActivityListener(IActivityListener listener);

    /**
     * @JTourBusStop 1, Activity sending, The IActivityProducer interface:
     * 
     *               Activities are used to exchange information in a session. A
     *               class that creates activities must implement this
     *               interface. Activities are "sent" by invoking
     *               IActivityListener.created() on the registered listeners.
     */

    /**
     * @JTourBusStop 1, Architecture Overview, User Interface -
     *               ActivityProducers:
     * 
     *               The "User Interface"-Component is responsible for managing
     *               all the visual elements in Saros, listening to changes in
     *               Eclipse and reacting to actions performed by the local
     *               user.
     * 
     *               Saros internally works with Activities. Activities are
     *               created by Activity Producers, which take local actions and
     *               turn them into Activities.
     * 
     */
}
