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

import de.fu_berlin.inf.dpp.intellij.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.SarosActionFactory;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.net.URL;

/**
 * Common class for Toolbar button implementations
 */
public abstract class ToolbarButton extends JButton {
    protected static final Logger LOG = Logger.getLogger(ToolbarButton.class);
    protected Saros saros = Saros.instance();


    /**
     * @param path
     * @param altText
     */
    protected void setIcon(String path, String altText) {
        setButtonIcon(this, path, altText);
    }


    /**
     * @param button
     * @param iconPath
     * @param altText
     */
    public static void setButtonIcon(JButton button, String iconPath, String altText) {
        if (!iconPath.startsWith("/"))
            iconPath = "/" + iconPath;

        URL imageURL = ToolbarButton.class.getResource(iconPath);
        if (imageURL != null) {
            //image found
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            //no image found
            button.setText(altText);
            LOG.error("Resource not found: " + imageURL);
        }
    }

    /**
     * @param actionCommand
     * @return
     */
    protected ISarosAction getAction(String actionCommand) {
        return SarosActionFactory.getAction(actionCommand);
    }

    /**
     *
     */
    protected void startAction() {
        startAction(getActionCommand());
    }

    /**
     * @param actionName
     */
    protected void startAction(String actionName) {
        SarosActionFactory.startAction(actionName);
    }

    /**
     * @param action
     */
    protected void startAction(ISarosAction action) {
        SarosActionFactory.startAction(action);
    }
}
