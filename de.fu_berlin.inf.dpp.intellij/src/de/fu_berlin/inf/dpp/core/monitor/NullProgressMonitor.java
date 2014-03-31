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

package de.fu_berlin.inf.dpp.core.monitor;

/**
 * Created by: r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.19 Time: 12.08
 */

public class NullProgressMonitor implements IProgressMonitor
{
    @Override
    public boolean isCanceled()
    {
        return false; // To change body of implemented methods use File |
        // Settings | File Templates.
    }

    @Override
    public void setCanceled(boolean cancel)
    {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void worked(int delta)
    {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void subTask(String remaingTime)
    {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void setTaskName(String name)
    {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void done()
    {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void beginTask(String taskName, String type)
    {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void beginTask(String taskNam, int size)
    {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public void internalWorked(double work)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
