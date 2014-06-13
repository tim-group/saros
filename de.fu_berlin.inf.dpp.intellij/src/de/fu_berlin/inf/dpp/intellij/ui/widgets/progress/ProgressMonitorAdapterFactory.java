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

package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;

//todo
public class ProgressMonitorAdapterFactory {

    /**
     * Convert an Eclipse ProgressMonitor to Saros Core ProgressMonitor
     *
     * @param monitor
     *            of Eclipse
     * @return converted ProgressMonitor
     */
    public static IProgressMonitor convertTo(
           IProgressMonitor monitor) {

       return monitor;
    }

    /**
     * Converts a Saros Core IProgressMonitor to a Eclipse ProgressMonitor
     *
     * @param monitor
     *            a Saros Core ProgressMonitor
     * @return the corresponding Eclipse
     *         {@linkplain org.eclipse.core.runtime.IProgressMonitor}
     */
    public static IProgressMonitor convertBack(
            IProgressMonitor monitor) {

       return monitor;
    }
}
