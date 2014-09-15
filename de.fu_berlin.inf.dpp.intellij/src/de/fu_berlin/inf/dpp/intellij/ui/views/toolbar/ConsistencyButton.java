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
 * Button for triggering a {@link ConsistencyAction}. Displays a different symbol
 * when state is inconsistent or not.
 */
public class ConsistencyButton extends ToolbarButton
{

    private static final String IN_SYNC_ICON_PATH = "icons/etool16/in_sync.png";
    private static final String OUT_SYNC_ICON_PATH = "icons/etool16/out_sync.png";
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isEnabled() && isInconsistent) {
                setEnabled(false);
                action.execute();
            }
        }
    };

    private boolean isInconsistent = false;

    private ConsistencyAction action;

    public ConsistencyButton()
    {
        super(ConsistencyAction.NAME, "Recover inconsistencies",
            IN_SYNC_ICON_PATH, "Files are consistent");
        action = (ConsistencyAction) SarosActionFactory.getAction(ConsistencyAction.NAME);
        action.setConsistencyButton(this);
        addActionListener(actionListener);
        setEnabled(false);
    }

    public void setInconsistent(boolean isInconsistent)
    {
        this.isInconsistent = isInconsistent;

        if (isInconsistent)
        {
            setEnabled(true);
            setIcon(OUT_SYNC_ICON_PATH, "Files are NOT consistent");
        }
        else
        {
            setEnabled(false);
            setIcon(IN_SYNC_ICON_PATH, "Files are consistent");
        }
    }
}
