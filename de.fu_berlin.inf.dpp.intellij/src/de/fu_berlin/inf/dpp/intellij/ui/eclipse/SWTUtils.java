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

import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.editor.mock.Display;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.concurrent.Callable;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-11
 * Time: 13:22
 */

public class SWTUtils
{
    public static Container getShell()
    {
        return Saros.instance().getMainPanel();
    }

    public static void runSafeSWTAsync(Logger log, Runnable runnable)
    {

        ThreadUtils.runSafeAsync(log, runnable);

    }

    public static void runSafeSWTSync(Logger log, Runnable runnable)
    {

        ThreadUtils.runSafeSync(log, runnable);

    }

    public static void runSWTSync(Logger log, Runnable runnable)
    {
        ThreadUtils.runSafeSync(log, runnable);
    }

    public static boolean isSWT()
    {
        return true;
    }

    public static Display getDisplay()
    {

        return Display.getDefault();
    }

    public static Object runSWTSync1(Callable<Object> callable) throws Exception
    {
        return callable.call();
    }

    public static Boolean runSWTSync(Callable<Boolean> callable) throws Exception
    {
        return callable.call();
    }
}
