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

package de.fu_berlin.inf.dpp.intellij.ui.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Saros Whiteboard panel
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class SarosWhiteboardView extends JFrame
{
    private Project project;
    private ToolWindow toolWindow;
    private Container parent;

    public SarosWhiteboardView(Project project, ToolWindow toolWindow) throws HeadlessException
    {
        this.project = project;
        this.toolWindow = toolWindow;

        this.parent = toolWindow.getComponent().getParent();

        create();
    }

    public SarosWhiteboardView() throws HeadlessException
    {
        super("Saros whiteboard panel");
        this.parent = this;

        create();
    }

    private void create()
    {
        //todo
    }
}
