package de.fu_berlin.inf.dpp.core.monitor;

/**
 * Created by IntelliJ IDEA. User: r.kvietkauskas Date: 14.3.14 Time: 13.11 To
 * change this template use File | Settings | File Templates.
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
}
