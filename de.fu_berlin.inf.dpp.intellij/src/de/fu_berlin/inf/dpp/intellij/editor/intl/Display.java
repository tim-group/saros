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

package de.fu_berlin.inf.dpp.intellij.editor.intl;

import de.fu_berlin.inf.dpp.intellij.editor.intl.exceptions.SWTException;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-15
 * Time: 13:31
 */

public class Display
{
    public static final Logger log = Logger.getLogger(Display.class);
    public boolean isDisposed()
    {
        //todo
        return false;
    }

    public void beep()
    {
        System.out.println("Display.beep");
    }

    public static Display getDefault()
    {
        System.out.println("Display.getDefault");
        //todo
        return new Display();
    }

    public static Display getCurrent()
    {
        return new Display();
    }

    public void asyncExec(Runnable runnable) throws SWTException
    {
        System.out.println("Display.asyncExec "+runnable);
        ThreadUtils.runSafeAsync(log, runnable);
    }

    public void syncExec(Runnable runnable)  throws SWTException
    {
        System.out.println("Display.syncExec "+runnable);
        ThreadUtils.runSafeSync(log, runnable);
    }
}
