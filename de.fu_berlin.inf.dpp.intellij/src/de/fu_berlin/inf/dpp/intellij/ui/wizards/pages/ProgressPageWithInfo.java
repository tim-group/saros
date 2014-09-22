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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import com.intellij.ui.components.JBScrollPane;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.MonitorProgressBar;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

/**
 * Wizard page with progress bar and info text area.
 */

public class ProgressPageWithInfo extends AbstractWizardPage
{
    private JProgressBar progressBar;
    private JLabel progressInfo;
    private MonitorProgressBar progressMonitor;


    private JTextArea display;
    private String title = "";
    private Color fontColor = Color.BLACK;

    /**
     * Constructor with custom ID
     *
     * @param fileListPageId
     * @param title identifier
     */
    public ProgressPageWithInfo(String fileListPageId, String title)
    {
        super(fileListPageId);
        this.title = title;
        create();
    }

    private void create()
    {
        setLayout(new BorderLayout());

        JPanel middlePanel = new JPanel();
        middlePanel.setBorder(new TitledBorder(new EtchedBorder(), title));

        display = new JTextArea(10, 48);
        display.setEditable(false);
        display.setForeground(fontColor);

        JScrollPane scroll = new JBScrollPane(display);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        middlePanel.add(scroll);

        add(middlePanel, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

        progressBar = new JProgressBar();

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        progressInfo = new JLabel("Starting");
        titlePanel.add(progressInfo);

        progressPanel.add(titlePanel);
        progressPanel.add(progressBar);
        progressPanel.add(new JLabel(" "));

        add(progressPanel, BorderLayout.SOUTH);
    }

    @Override
    public void aboutToHidePanel()
    {
        display.setText("");

        if (progressMonitor != null)
        {
            progressMonitor.done();
            progressMonitor = null;
        }
    }

    /**
     * Adds text paragraph
     *
     * @param text
     */
    public void addLine(final String text)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                display.append(text + "\n\r");
            }
        });
    }

    /**
     * Creates Progress monitor.
     *
     * @param indeterminate use indeterminate
     * @param closeOnFinish close wizard when finished
     * @return IProgressMonitor
     */
    public IProgressMonitor getProgressMonitor(boolean indeterminate, boolean closeOnFinish)
    {
        if (progressMonitor == null)
        {
            MonitorProgressBar progress = new MonitorProgressBar(progressBar, progressInfo);
            if (indeterminate)
            {
                progress.beginTask("starting", IProgressMonitor.UNKNOWN);
            }


            if (closeOnFinish)
            {
                progress.setFinishListener(new MonitorProgressBar.FinishListener()
                {
                    @Override
                    public void finished()
                    {
                        wizard.close();
                    }
                });
            }

            progressMonitor = progress;
        }

        return progressMonitor;
    }

    @Override
    public boolean isBackButtonVisible() {
        return false;
    }

    @Override
    public boolean isNextButtonVisible() {
        return false;
    }
}
