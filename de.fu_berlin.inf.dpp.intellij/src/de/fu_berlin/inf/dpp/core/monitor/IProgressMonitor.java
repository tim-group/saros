package de.fu_berlin.inf.dpp.core.monitor;

/**
 * Temporary interface for compatibility with Eclipse SubMonitor implementation
 */
//todo: remove it and use de.fu_berlin.inf.dpp.monitoring.IProgressMonitor in all IntelliJ classes
public interface IProgressMonitor
    extends de.fu_berlin.inf.dpp.monitoring.IProgressMonitor {

    void beginTask(String taskName, String type);

    void internalWorked(double work);

    ISubMonitor convert();

    ISubMonitor convert(String title, int progress);
}
