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

package de.fu_berlin.inf.dpp.intellij.runtime;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.SarosProgressMonitor;

/**
 * Class designed to start long lasting job with progress indicator
 */
public abstract class UIMonitoredJob extends Thread
{

    private IProgressMonitor monitor;

    public UIMonitoredJob(String name, IProgressMonitor monitor)
    {
        super(name);
        if (monitor == null)
        {
            this.monitor = new SarosProgressMonitor();
        }
        else
        {
            this.monitor = monitor;
        }
    }

    /**
     * Creates job with named progress window
     *
     * @param name progress window name
     */
    public UIMonitoredJob(final String name)
    {
        super(name);
        monitor = new SarosProgressMonitor();
        monitor.setTaskName(name);


    }


    public void schedule()
    {
        start();
    }


    @Override
    public final void run()
    {

        run(monitor);
    }

    /**
     * Implement job business logic here.
     * IProgressMonitor is passed internally.
     * Implementation is responsible to pass information about progress for progress monitor
     *
     * @param monitor
     * @return
     */
    protected abstract IStatus run(IProgressMonitor monitor);

}
