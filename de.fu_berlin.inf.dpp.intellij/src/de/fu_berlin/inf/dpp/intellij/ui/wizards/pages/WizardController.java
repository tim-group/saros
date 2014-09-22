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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Wizard controller
 */
public class WizardController implements ActionListener
{
    private Wizard wizard;

    public WizardController(Wizard wizard)
    {
        this.wizard = wizard;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {

        if (wizard.getWizardPageModel().getCurrentPage() == null)
        {
            return;
        }

        if (Wizard.NEXT_ACTION.equalsIgnoreCase(e.getActionCommand()))
        {
            wizard.getWizardPageModel().getCurrentPage().actionNext();
            AbstractWizardPage nextPage = wizard.getWizardPageModel().getNextPage();
            if (nextPage == null)
            {
                wizard.getWizardPageModel().getCurrentPage().aboutToHidePanel();
                wizard.close();
            }
            else
            {
                wizard.setCurrentPage(nextPage);
            }

        }
        else if (Wizard.BACK_ACTION.equalsIgnoreCase(e.getActionCommand()))
        {
            wizard.getWizardPageModel().getCurrentPage().actionBack();
            wizard.setCurrentPage(wizard.getWizardPageModel().getBackPage());
        }
        else if (Wizard.CANCEL_ACTION.equalsIgnoreCase(e.getActionCommand()))
        {
            wizard.getWizardPageModel().getCurrentPage().actionCancel();
            this.wizard.close();
        }


    }
}
