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


import javax.swing.*;
import java.awt.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-11
 * Time: 15:42
 */

public class MessageDialog
{

    public static void openInformation(Component shell,  String title, String message)
    {
        JOptionPane.showInternalMessageDialog(shell,title,message,JOptionPane.INFORMATION_MESSAGE);
    }

    public static void openError(Component shell,  String title, String message)
    {
        JOptionPane.showInternalMessageDialog(shell,title,message,JOptionPane.ERROR_MESSAGE);
    }



    public static boolean openConfirm(Component shell, String title, String message)
    {
        int resp = JOptionPane.showConfirmDialog(shell, title, message, JOptionPane.YES_OPTION);
        return resp == 0;
    }

    public static boolean openQuestion(Container shell, String title, String message)
    {
        return openConfirm(shell, title, message);
    }
}
