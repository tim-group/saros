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

import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.SarosActionFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * General button used to create any common button
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.21
 * Time: 09.52
 */

public class CommonButton extends ToolbarButton implements ActionListener
{
    private String actionCommand;
    private ISarosAction sarosAction;
    private String altText;
    private String tooltipText;
    private String iconName;

    /**
     * @param actionCommand
     * @param tooltipText
     * @param iconName
     * @param altText
     */
    public CommonButton(String actionCommand, String tooltipText, String iconName, String altText)
    {
        this.actionCommand = actionCommand;
        this.altText = altText;
        this.tooltipText = tooltipText;
        this.iconName = iconName;

        this.sarosAction = SarosActionFactory.getAction(actionCommand);
        sarosAction.setGuiFrame(this);

        create();
    }

    /**
     * @param sarosAction
     * @param altText
     * @param tooltipText
     * @param iconName
     */
    public CommonButton(ISarosAction sarosAction, String tooltipText, String iconName, String altText)
    {
        this.sarosAction = sarosAction;
        this.altText = altText;
        this.tooltipText = tooltipText;
        this.iconName = iconName;
        this.actionCommand = sarosAction.getActionName();
        sarosAction.setGuiFrame(this);

        create();
    }

    protected void create()
    {
        setIcon(this.iconName, this.altText);
        setActionCommand(this.actionCommand);

        setToolTipText(this.tooltipText);

        addActionListener(this);
        this.sarosAction.setGuiFrame(this);
    }

    public String getActionCommand()
    {
        return actionCommand;
    }

    public ISarosAction getSarosAction()
    {
        return sarosAction;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        startAction(sarosAction);
    }
}
