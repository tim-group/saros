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

package de.fu_berlin.inf.dpp.intellij.ui.eclipse;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.SarosProgressMonitor;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.28
 * Time: 11.11
 */

public abstract class Job extends Thread
{
    public static final int SHORT = 1;

    private boolean isUser;
    private IProgressMonitor monitor;

    protected Job(String name, IProgressMonitor monitor)
    {
        super(name);
        this.monitor = monitor;
    }

    public Job(String name)
    {
        super(name);
        this.monitor = new SarosProgressMonitor(name);

        System.out.println("Job.Job");
    }

    public void setUser(boolean isUser)
    {
        this.isUser = isUser;
    }

    public boolean isUser()
    {
        return isUser;
    }

    public void schedule()
    {
        start();
    }


    @Override
    public void run()
    {
        if (monitor == null)
        {
            monitor = new NullProgressMonitor();
        }

        run(monitor);
    }

    protected abstract IStatus run(IProgressMonitor monitor);

    public void setProperty(String key, Object value)
    {

    }

}
