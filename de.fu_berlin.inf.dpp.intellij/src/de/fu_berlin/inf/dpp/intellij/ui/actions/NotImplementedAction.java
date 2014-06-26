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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;

/**
 * Class used to emulate not implemented actions
 * <p/>
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class NotImplementedAction extends AbstractSarosAction
{
    public enum actions
    {
        preferences
    }

    private String name;

    public NotImplementedAction(actions enAction)
    {
        this.name = enAction.name();
    }

    public NotImplementedAction(String name)
    {
        this.name = name;
    }

    @Override
    public String getActionName()
    {
        return name;
    }


    @Override
    public void run()
    {
        actionStarted();

        LOG.info("Not implemented action [" + name + "]");

        SafeDialogUtils.showError("We are sorry, but action [" + name + "] not implemented yet!", "Not Implemented");

        actionFinished();
    }
}
