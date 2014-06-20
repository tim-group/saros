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

package de.fu_berlin.inf.dpp.intellij.ui.eclipse;

import de.fu_berlin.inf.dpp.intellij.ui.IWizard;
import de.fu_berlin.inf.dpp.intellij.ui.IWizardDialogAccessible;

import javax.swing.*;
import java.awt.*;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-11
 * Time: 13:07
 */

public class WizardDialogAccessible extends WizardDialog implements IWizardDialogAccessible
{
    public WizardDialogAccessible(Container parentShell, IWizard newWizard)
    {
        super(parentShell, newWizard);
    }

    public WizardDialogAccessible(Container parentShell, IWizard newWizard,
            int includeStyle, int excludeStyle)
    {
        super(parentShell, newWizard);


        setShellStyle(getShellStyle() | includeStyle);
        setShellStyle(getShellStyle() & (~excludeStyle));
    }

    public JButton getWizardButton(int id)
    {
        return getButton(id);
    }

    public void setWizardButtonLabel(int id, String label)
    {
        JButton btn = getButton(id);
        if (btn != null)
        {
            btn.setText(label);
        }
    }

    public void setWizardButtonEnabled(int id, boolean enabled)
    {
        JButton btn = getButton(id);
        if (btn != null)
        {
            btn.setEnabled(enabled);
        }
    }


}

