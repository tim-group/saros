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

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.intellij.ui.IWizard;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-11
 * Time: 13:08
 */

public class WizardDialog extends JComponent
{

    private boolean blockOnOpen;
    private boolean helpAvailable;
    private Container parent;
    private IWizard newWizard;

    private List<JButton> buttonList = new ArrayList<JButton>();

    public WizardDialog()
    {
    }

    public WizardDialog(Container parent, IWizard newWizard)
    {
        this.parent = parent;
        this.newWizard = newWizard;
    }

    protected JButton getButton(int id)
    {
        return buttonList.get(id);
    }

    protected void buttonPressed(int buttonId)
    {
        JButton btn = buttonList.get(buttonId);

        //todo
        System.out.println("WizardDialog.buttonPressed //todo");
    }

    public IProgressMonitor getProgressMonitor()
    {
        return new NullProgressMonitor();
    }

    private int shellStyle;

    protected int getShellStyle()
    {

        return shellStyle;
    }

    protected void setShellStyle(int style)
    {
        this.shellStyle = style;
    }

    public void close()
    {
        System.out.println("WizardDialog.close");
        super.setVisible(false); //todo
    }

    public boolean isBlockOnOpen()
    {
        return blockOnOpen;
    }

    public void setBlockOnOpen(boolean blockOnOpen)
    {
        this.blockOnOpen = blockOnOpen;
    }

    public boolean isHelpAvailable()
    {
        return helpAvailable;
    }

    public void setHelpAvailable(boolean helpAvailable)
    {
        this.helpAvailable = helpAvailable;
    }
}
