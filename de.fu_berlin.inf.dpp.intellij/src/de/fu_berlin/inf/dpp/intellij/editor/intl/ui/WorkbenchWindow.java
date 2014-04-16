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

package de.fu_berlin.inf.dpp.intellij.editor.intl.ui;

import de.fu_berlin.inf.dpp.intellij.editor.intl.Display;
import de.fu_berlin.inf.dpp.intellij.editor.intl.events.IWindowListener;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-15
 * Time: 17:23
 */

public class WorkbenchWindow implements IWorkbenchWindow
{

    @Override
    public IWorkbenchWindow getActiveWorkbenchWindow()
    {
        System.out.println("WorkbenchWindow.getActiveWorkbenchWindow //todo");
        //todo
        return new WorkbenchWindow();
    }

    @Override
    public IWorkbenchWindow[] getWorkbenchWindows()
    {
        System.out.println("WorkbenchWindow.getWorkbenchWindows //todo");
        //todo
        return new IWorkbenchWindow[0];
    }

    @Override
    public IWorkbenchPage getActivePage()
    {
        System.out.println("WorkbenchWindow.getActivePage //todo");
        //todo
        return new WorkbenchPage();
    }

    @Override
    public IWorkbenchWindow getPartService()
    {
        System.out.println("WorkbenchWindow.getPartService //todo");
        //todo
        return new WorkbenchWindow();
    }

    @Override
    public IWorkbenchPage[] getPages()
    {
        System.out.println("WorkbenchWindow.getPages //todo");
        return new IWorkbenchPage[0];
    }

    @Override
    public void addPartListener(IPartListener2 partListener)
    {
        //todo
        System.out.println("WorkbenchWindow.addPartListener //todo");
    }

    @Override
    public void removePartListener(IPartListener2 partListener)
    {
        //todo
        System.out.println("WorkbenchWindow.removePartListener //todo");
    }

    @Override
    public boolean isClosing()
    {
        return false;
    }

    @Override
    public Display getDisplay()
    {
        return new Display();
    }

    @Override
    public void addWindowListener(IWindowListener listener)
    {
        //todo
        System.out.println("WorkbenchWindow.addWindowListener //todo");
    }

    @Override
    public void removeWindowListener(IWindowListener listener)
    {
        //todo
        System.out.println("WorkbenchWindow.removeWindowListener //todo");
    }

}
