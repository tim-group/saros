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

import de.fu_berlin.inf.dpp.intellij.core.Saros;

import javax.swing.*;
import java.awt.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-11
 * Time: 13:26
 */

public class DialogUtils
{
    private static final Container container = Saros.instance().getMainPanel();

    public static void openWindow(Container c)
    {
        System.out.println("DialogUtils.openWindow");
        if (c.isVisible())
        {
            c.setVisible(true);
        }
    }

    public static void openInformationMessageDialog(Container shell, String msg, String title)
    {

        JOptionPane.showConfirmDialog(shell, msg, title, JOptionPane.OK_OPTION);
    }

    public static void openWarningMessageDialog(Component shell, String msg, String title)
    {

        JOptionPane.showInternalMessageDialog(shell, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void popUpFailureMessage(String msg, String title, boolean b)
    {

        openErrorMessageDialog(getDefaultContainer(), msg, title);

    }

    public static void openErrorMessageDialog(Component parent, String msg, String title)
    {

        JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean openQuestionMessageDialog(Component parent, String msg, String title)
    {
        int answer = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_OPTION);

        return answer == 0;
    }

    public static Container getDefaultContainer()
    {
        return container;
    }
}
