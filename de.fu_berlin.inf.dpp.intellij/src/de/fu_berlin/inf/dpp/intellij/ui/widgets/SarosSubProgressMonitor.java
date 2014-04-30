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

package de.fu_berlin.inf.dpp.intellij.ui.widgets;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-25
 * Time: 13:29
 */

public class SarosSubProgressMonitor extends SarosProgressMonitor implements ISubMonitor
{
    private IProgressMonitor mainMonitor;

    public SarosSubProgressMonitor(String title, IProgressMonitor mainMonitor)
    {
        super(title);
        this.mainMonitor = mainMonitor;
    }

    @Override
    public ISubMonitor newChild(int id)
    {
        return this;
    }

    @Override
    public IProgressMonitor getMain()
    {

        return mainMonitor;
    }

    @Override
    public IProgressMonitor newChildMain(int progress)
    {
        if(mainMonitor instanceof SarosProgressMonitor)
        {
            ((SarosProgressMonitor)mainMonitor).setProgress(progress);
        }


        return mainMonitor;
    }

    @Override
    public IProgressMonitor newChildMain(int progress, int mode)
    {
        return newChildMain(progress);
    }

    @Override
    public ISubMonitor newChild(int progress, int mode)
    {
        SarosSubProgressMonitor subMonitor = new SarosSubProgressMonitor(title,mainMonitor);
        subMonitor.setProgress(progress);
        return subMonitor;
    }
}
