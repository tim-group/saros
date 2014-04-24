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

package de.fu_berlin.inf.dpp.intellij.ui.util;

import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.intellij.core.Saros;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-17
 * Time: 16:31
 */

public class DialogUtil
{
    private static Saros saros = Saros.instance();

    /**
     *
     * @param message
     * @param initialValue
     * @return
     */
    public static String showInputDialog(final String message, final String initialValue)
    {
        final StringBuilder response = new StringBuilder();
        Runnable action = new Runnable()
        {
           @Override
            public void run()
            {
                if (saros.getProject() == null)
                {
                    String option = JOptionPane.showInputDialog(saros.getMainPanel(), message, initialValue);
                    response.append(option);
                }
                else
                {
                    String option = Messages.showInputDialog(saros.getProject(), message, initialValue, Messages.getQuestionIcon());
                    response.append(option);
                }
            }

        } ;

        if(saros.getProject()==null)
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                action.run();
            }
            else
            {
                try
                {
                    SwingUtilities.invokeAndWait(action);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            UIUtil.invokeAndWaitIfNeeded(action);
        }
        return response.toString();
    }

    /**
     *
     * @param message
     * @param title
     */
    public static void showError(final String message, final String title)
    {
        Runnable action = new Runnable()
        {
            @Override
            public void run()
            {
                if (saros.getProject() == null)
                {
                    JOptionPane.showMessageDialog(saros.getMainPanel(), message, title, JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    Messages.showErrorDialog(saros.getProject(), message, title);
                }
            }
        };

        if(saros.getProject()==null)
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                action.run();
            }
            else
            {
                try
                {
                    SwingUtilities.invokeAndWait(action);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            UIUtil.invokeAndWaitIfNeeded(action);
        }
    }
}
