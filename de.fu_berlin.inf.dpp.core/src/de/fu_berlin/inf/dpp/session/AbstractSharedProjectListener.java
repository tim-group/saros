package de.fu_berlin.inf.dpp.session;


/**
 * Abstract {@link ISharedProjectListener} that does nothing in all the methods.
 * 
 * Clients can override just the methods they want to act upon.
 */
public abstract class AbstractSharedProjectListener implements
    ISharedProjectListener {

    @Override
    public void permissionChanged(User user) {
        // Do nothing.
    }

    @Override
    public void userJoined(User user) {
        // Do nothing.
    }

    @Override
    public void userStartedQueuing(User user) {
        // Do nothing.
    }

    @Override
    public void userFinishedProjectNegotiation(User user) {
        // Do nothing
    }

    @Override
    public void userLeft(User user) {
        // Do nothing.
    }
}
