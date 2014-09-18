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

import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.MonitorProgressBar;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.AbstractWizardPage;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Standard progress bar panel
 *
 */
public class ProgressPage extends AbstractWizardPage
{
    private JProgressBar progressBar;
    private JLabel progressInfo;
    protected MonitorProgressBar progressMonitor;

    /**
     * Constructor with custom ID
     *
     * @param id identifier
     */
    public ProgressPage(String id)
    {
        super(id);
        create();
    }

    /**
     * Constructor with default ID
     */
    public ProgressPage()
    {
        super("Progress");
        create();
    }

    /**
     * Creates UI
     */
    protected void create()
    {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

        this.progressBar = new JProgressBar();

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        this.progressInfo = new JLabel("Starting");
        titlePanel.add(this.progressInfo);

        progressPanel.add(titlePanel);
        progressPanel.add(progressBar);
        progressPanel.add(new JLabel(" "));

        setLayout(new BorderLayout());
        add(progressPanel, BorderLayout.SOUTH);
    }


    public JProgressBar getProgressBar()
    {
        return progressBar;
    }

    public JLabel getProgressInfo()
    {
        return progressInfo;
    }

    @Override
    public String getNextButtonTitle()
    {
        return null;
    }

    @Override
    public void displayingPanel()
    {
        wizard.getNavigationPanel().setVisibleBack(false);
        wizard.getNavigationPanel().setVisibleNext(false);
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
            MonitorProgressBar progress = new MonitorProgressBar(getProgressBar(), getProgressInfo());

            if(indeterminate)
            progress.beginTask("starting", de.fu_berlin.inf.dpp.monitoring.IProgressMonitor.UNKNOWN);

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
}
