package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

public interface IActivityProducer {
    /**
     * Executes the given activity.
     * 
     * @swt The implementor may expect that this method is called from the SWT
     *      thread.
     *      <p>
     *      TODO What is the 'Saros core' equivalent of the SWT thread?
     */
    public void exec(IActivity activity);
}
