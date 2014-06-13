package de.fu_berlin.inf.dpp.core.monitor;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.28
 * Time: 11.08
 */

public interface IProgressMonitor extends de.fu_berlin.inf.dpp.monitoring.IProgressMonitor
{
    public static final int UNKNOWN = 0;


    void beginTask(String taskName, String type);

    void internalWorked(double work);

    ISubMonitor convert();

    ISubMonitor convert(String title, int progress);
}
