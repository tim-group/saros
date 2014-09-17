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
 *
 */

package de.fu_berlin.inf.dpp.intellij.ui.views;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;

/**
 * Saros main panel view
 */
public class SarosMainPanelView extends JFrame {
    protected static final Logger LOG = Logger
        .getLogger(SarosMainPanelView.class);

    private final Container parent;

    public SarosToolbar getSarosToolbar() {
        return sarosToolbar;
    }

    private SarosToolbar sarosToolbar;
    private SarosTreeView sarosTree;

    @Inject
    private Saros saros;

    static {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            LOG.debug("Look&feel " + UIManager
                .getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            LOG.error("Could not set Look&feel !", e);
        }
    }

    public SarosMainPanelView(ToolWindow toolWindow) throws HeadlessException {
        SarosPluginContext.initComponent(this);

        saros.setMainPanel(this);

        if (toolWindow != null) {
            parent = toolWindow.getComponent().getParent();
        } else {
            parent = this;
            setTitle("Saros panel");
        }
        create();
    }

    public JComponent create() {

        LOG.info("Plugin started in [" + new File("").getAbsolutePath()
            + "] directory");

        sarosTree = new SarosTreeView();
        this.add(sarosTree.getRootTree().getJtree());
        sarosToolbar = new SarosToolbar(this);

        Container sessionPane = new JPanel(new BorderLayout());

        JTree tree = sarosTree.create();
        JScrollPane treeView = new JBScrollPane(tree);
        treeView.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        treeView.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sessionPane.add(treeView);

        Container chartPane = new JPanel(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            sessionPane, chartPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(350);

        //Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(300, 50);
        sessionPane.setMinimumSize(minimumSize);
        splitPane.setMinimumSize(minimumSize);

        JPanel sarosPanel = new JPanel(new BorderLayout());
        sarosPanel.add(splitPane, BorderLayout.CENTER);
        sarosPanel.add(sarosToolbar.getjToolBar(), BorderLayout.NORTH);

        return sarosPanel;
    }

    public SarosTreeView getSarosTree() {
        return sarosTree;
    }

    @Override public Container getParent() {
        return parent;
    }
}
