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

package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosMainPanelView;

/**
 * Saros core panel tool window factory. Here is a starting point of IntelliJ plugin
 * <p/>
 */

public class SarosToolWindowFactory implements ToolWindowFactory {

    private SarosMainPanelView sarosMainPanelView;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        sarosMainPanelView = new SarosMainPanelView();
        Content content = toolWindow.getContentManager().getFactory().createContent(
            sarosMainPanelView, "Saros", false);
        toolWindow.getContentManager().addContent(content);
    }
}
