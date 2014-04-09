package de.fu_berlin.inf.dpp.core.monitor;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.28
 * Time: 11.08
 */

public interface IProgressMonitor
{
    public static final int UNKNOWN = 0;

    boolean isCanceled();

    void setCanceled(boolean cancel);

    void worked(int delta);

    void subTask(String remaingTime);

    void setTaskName(String name);

    void done();

    void beginTask(String taskName, String type);

    void beginTask(String taskNam, int size);

    void internalWorked(double work);

    ISubMonitor convert(IProgressMonitor monitor);

    ISubMonitor convert(IProgressMonitor monitor, String title, int progress);
}
