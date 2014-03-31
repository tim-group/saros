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

package de.fu_berlin.inf.dpp.intellij.ui;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.vcs.ISubMonitor;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 15.24
 */

public class SubMonitor implements ISubMonitor
{
    IProgressMonitor mainMonitor;

    public SubMonitor()
    {
    }

    public SubMonitor(IProgressMonitor mainMonitor)
    {
        this.mainMonitor = mainMonitor;
    }

    @Override
    public void subTask(String name)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void done()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void beginTask(String taskName, String type)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ISubMonitor newChild(int id)
    {
        return this; //todo
    }

    @Override
    public IProgressMonitor getMain()
    {
        return mainMonitor;
    }

    @Override
    public IProgressMonitor newChildMain(int progress)
    {
        return this;
    }

    @Override
    public IProgressMonitor newChildMain(int progress, int mode)
    {
        return this;
    }

    @Override
    public ISubMonitor newChild(int progress, int mode)
    {
        return this; //todo
    }

    @Override
    public boolean isCanceled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTaskName(String name)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void beginTask(String taskName, int workTotal)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void internalWorked(double work)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void worked(int worked)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCanceled(boolean cancel)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
