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

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-15
 * Time: 15:06
 */

public interface IPartListener2
{

    public void partActivated(IWorkbenchPartReference partRef) ;


    public void partOpened(IWorkbenchPartReference partRef);


    public void partClosed(IWorkbenchPartReference partRef);

    /**
     * We need to catch partBroughtToTop events because partActivate events are
     * missing if Editors are opened programmatically.
     */
    public void partBroughtToTop(IWorkbenchPartReference partRef);


    public void partDeactivated(IWorkbenchPartReference partRef);


    public void partHidden(IWorkbenchPartReference partRef);


    public void partVisible(IWorkbenchPartReference partRef);
    /**
     * Called for instance when a file was renamed. We just close and open the
     * editor.
     */
    public void partInputChanged(IWorkbenchPartReference partRef);
}