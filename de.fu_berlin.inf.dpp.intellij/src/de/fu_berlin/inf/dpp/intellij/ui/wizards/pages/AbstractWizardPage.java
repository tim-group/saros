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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import de.fu_berlin.inf.dpp.intellij.ui.wizards.Wizard;

import javax.swing.JPanel;


/**
 * Abstract base class for wizard pages.
 */
public abstract class AbstractWizardPage extends JPanel
{
    protected Wizard wizard;
    private String id;
    private PageActionListener actionListener;

    /**
     * Creates new wizard page with new identification ID
     *
     * @param id identification
     */
    public AbstractWizardPage(String id)
    {
        this.id = id;
    }

    /**
     *  Returns unique identifier within wizard
     *
     * @return id
     */
    public String getId()
    {
        return id;
    }

    /**
     * TItle of the next button for this page (e.g. next or accept).
     *
     * @return {@link NavigationPanel#TITLE_NEXT}.
     */
    public String getNextButtonTitle()
    {
        return NavigationPanel.TITLE_NEXT;
    }

    /**
     *
     * @param wizard
     */
    public void setWizard(Wizard wizard)
    {
        this.wizard = wizard;
    }

    /**
     * Action performed when user clicks on on Back button
     */
    public void actionBack()
    {
        if (actionListener != null)
        {
            actionListener.back();
        }
    }

    /**
     * Sets page action listener
     *
     * @param actionListener page action listener
     */
    public void addPageListener(PageActionListener actionListener)
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
    public void actionNext()
    {
        if (actionListener != null)
        {
            actionListener.next();
        }
    }

    public boolean isBackButtonVisible() {
        return true;
    }

    public boolean isNextButtonVisible() {
        return true;
    }
}
