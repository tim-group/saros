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

package de.fu_berlin.inf.dpp.intellij.ui.views.toolbar;

import de.fu_berlin.inf.dpp.intellij.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.SarosActionFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Consistency check button implementation
 */
public class ConsistencyButton extends ToolbarButton implements ActionListener
{


    private static final String IN_SYNC_ICON_PATH = "icons/etool16/in_sync.png";
    private static final String OUT_SYNC_ICON_PATH = "icons/etool16/out_sync.png";

    private boolean isInconsistent = false;

    private ConsistencyAction action;

    public ConsistencyButton()
    {

        this.action = (ConsistencyAction) SarosActionFactory.getAction(ConsistencyAction.NAME);
        this.action.setConsistencyButton(this);
        setActionCommand(action.getActionName());
        // super.setEnabled(false);
        addActionListener(this);

        setInconsistent(false);

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (isEnabled() && isInconsistent)
        {
            setEnabled(false);
            action.execute();
        }

    }

    public void setInconsistent(boolean isInconsistent)
    {
        this.isInconsistent = isInconsistent;

        if (isInconsistent)
        {
            setIcon(OUT_SYNC_ICON_PATH, "Files are NOT consistent");
        }
        else
        {
            setIcon(IN_SYNC_ICON_PATH, "Files are consistent");
        }
    }

    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
    }
}
