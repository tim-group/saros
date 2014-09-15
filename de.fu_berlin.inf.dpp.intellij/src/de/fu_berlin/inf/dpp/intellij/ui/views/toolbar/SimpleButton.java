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

import de.fu_berlin.inf.dpp.intellij.ui.actions.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.SarosActionFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple button used to create actions that just call {@link AbstractSarosAction#execute()}.
 */
public class SimpleButton extends ToolbarButton
{
    private AbstractSarosAction action;

    public SimpleButton(String actionCommand, String tooltipText, String iconPath, String altText) {
        super(actionCommand, tooltipText, iconPath, altText);

        action = SarosActionFactory.getAction(actionCommand);
        addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                action.execute();
            }
        });
    }
}
