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

import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.SarosActionFactory;
import de.fu_berlin.inf.dpp.intellij.util.SarosResourceLocator;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.net.URL;

/**
 * Common class for Toolbar button implementations
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.21
 * Time: 07.39
 */
public abstract class ToolbarButton extends JButton
{
    protected static final Logger log = Logger.getLogger(ToolbarButton.class);
    protected Saros saros = Saros.instance();

    /**
     * @param name
     * @param altText
     */
    protected void setIcon(String name, String altText)
    {
        setButtonIcon(this, name, altText);
    }

    /**
     * @param button
     * @param iconName
     * @param altText
     */
    public static void setButtonIcon(JButton button, String iconName, String altText)
    {
        URL imageURL = SarosResourceLocator.getButtonImageUrl(iconName);
        if (imageURL != null)
        {
            //image found
            button.setIcon(new ImageIcon(imageURL, altText));
        }
        else
        {
            //no image found
            button.setText(altText);
            log.error("Resource not found: " + imageURL);
        }
    }

    /**
     * @param actionCommand
     * @return
     */
    protected ISarosAction getAction(String actionCommand)
    {
        return SarosActionFactory.getAction(actionCommand);
    }

    /**
     *
     */
    protected void startAction()
    {
        startAction(getActionCommand());
    }

    /**
     * @param actionName
     */
    protected void startAction(String actionName)
    {
        SarosActionFactory.startAction(actionName);
    }

    /**
     * @param action
     */
    protected void startAction(ISarosAction action)
    {
        SarosActionFactory.startAction(action);
    }
}
