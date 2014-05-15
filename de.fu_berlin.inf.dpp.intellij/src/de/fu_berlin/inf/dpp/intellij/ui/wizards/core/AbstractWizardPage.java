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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.core;

import javax.swing.*;


/**
 * Top class for wizard pages
 *
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-14
 * Time: 08:08
 */

public abstract class AbstractWizardPage extends JPanel
{
    protected Wizard wizard;
    private Object id;
    private PageActionListener actionListener;

    /**
     * Creates new wizard page with new identification ID
     *
     * @param id identification
     */
    public AbstractWizardPage(Object id)
    {
        this.id = id;

        create();
    }

    /**
     * Creates UI. Should be overwritten in inherited calss
     *
     */
    protected void create()
    {
        add(new JLabel("Wizard page " + id));
    }

    /**
     *  Returns unique identifier within wizard
     *
     * @return id
     */
    public Object getId()
    {
        return id;
    }

    /**
     * Method started before hiding panel
     */
    public void aboutToHidePanel()
    {

    }

    /**
     * Method started before displaying panel
     */
    public void aboutToDisplayPanel()
    {

    }

    /**
     * Method started after displaying panel
     */
    public void displayingPanel()
    {

    }

    /**
     *
     * @return
     */
    public String getNextButtonTitle()
    {
        return NavigationPanel.TITLE_NEXT;
    }

    /**
     *
     * @param wizard
     */
    protected void setWizard(Wizard wizard)
    {
        this.wizard = wizard;
    }

    /**
     * Action performed when user clicks on on Back button
     */
    protected void actionBack()
    {
        actionCancel();
    }

    /**
     * Sets page action listener
     *
     * @param actionListener page action listener
     */
    public void setActionListener(PageActionListener actionListener)
    {
        this.actionListener = actionListener;
    }

    /**
     *  Action performed when user clicks Cancel button
     */
    public void actionCancel()
    {
        if (actionListener != null)
        {
            actionListener.cancel();
        }
    }

    /**
     * Action performed when user clicks on Next button
     */
    protected void actionNext()
    {
        if (actionListener != null)
        {
            actionListener.accept();
        }
    }
}
