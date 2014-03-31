package de.fu_berlin.inf.dpp.session;

/**
 * TODO Formulate a sound description for this interface as its ancestor
 * (IActivityProvider) allows now straightforward splitting.
 */
public interface IActivityConsumer {
    /**
     * Registers the given listener, so it will be informed via
     * {@link IActivityListener#activityCreated(de.fu_berlin.inf.dpp.activities.business.IActivity)
     * IActivityListener.activityCreated()} when this IActivityConsumer received
     * an Activity.
     */
    public void addActivityListener(IActivityListener listener);

    /**
     * Removes a listener previously registered with addActivityListener.
     */
    public void removeActivityListener(IActivityListener listener);
}
