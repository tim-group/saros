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

package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Creates progress monitor panel
 *
 *
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-12
 * Time: 14:45
 */

public class SarosProgressMonitor implements ISubMonitor
{
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 100;

    public static final String TITLE = "Progress monitor";
    public static final String BUTTON_CANCEL = "Cancel";

    private Container parent; // = Saros.instance().getMainPanel();
    private JFrame frmMain;


    private JButton btnCancel;
    private JProgressBar progressBar;
    private JLabel fieldInfo;

    private boolean isCanceled = false;

    private int progress = 0;

    /**
     * Constructor with default title
     */
    public SarosProgressMonitor()
    {
        this(TITLE);
    }

    /**
     * Constructor with explicit title
     *
     * @param title dialog title
     */
    public SarosProgressMonitor(String title)
    {

        frmMain = new JFrame(title);
        frmMain.setSize(300, 160);
        frmMain.setLocationRelativeTo(parent);

        Container pane = frmMain.getContentPane();
        pane.setLayout(null);

        frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        fieldInfo = new JLabel();
        btnCancel = new JButton(BUTTON_CANCEL);
        btnCancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setCanceled(true);
            }
        });

        progressBar = new JProgressBar(MIN_VALUE, MAX_VALUE);

        pane.add(fieldInfo);
        pane.add(btnCancel);
        pane.add(progressBar);

        fieldInfo.setBounds(10, 15, 200, 15);
        progressBar.setBounds(10, 50, 280, 20);
        btnCancel.setBounds(100, 85, 100, 25);

        frmMain.setResizable(false);
        frmMain.setVisible(true);

        this.frmMain.repaint();

    }

    /**
     * Sets progress note to UI
     *
     * @param note progress note
     */
    private void setNoteInt(final String note)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                fieldInfo.setText(note);
            }
        });

    }

    /**
     * Sets progress info to UI
     *
     * @param progress
     */
    private void setProgressInt(final int progress)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                progressBar.setValue(progress);
            }
        });

    }

    /**
     * Checks progress bar state
     *
     * @return boolean
     */
    private boolean isRunning()
    {
        return !isCanceled && progressBar.getValue() < MAX_VALUE;
    }


    public void setProgress(int progress)
    {
        this.progress = progress;
        setProgressInt(progress);
    }

    @Override
    public void subTask(String name)
    {
        setNoteInt(name);
    }

    @Override
    public void done()
    {
        frmMain.dispose();
    }

    @Override
    public void beginTask(String taskName, String type)
    {
        setNoteInt(taskName);
    }

    @Override
    public ISubMonitor newChild(int id)
    {
        return this;
    }

    @Override
    public IProgressMonitor getMain()
    {
        return this;
    }

    @Override
    public IProgressMonitor newChildMain(int progress)
    {
        setProgress(progress);
        return this;
    }

    @Override
    public IProgressMonitor newChildMain(int progress, int mode)
    {
        setProgress(progress);
        return this;
    }

    @Override
    public ISubMonitor newChild(int progress, int mode)
    {
        setProgress(progress);
        return this;
    }

    @Override
    public boolean isCanceled()
    {
        return isCanceled;
    }

    @Override
    public void setTaskName(String name)
    {
        setNoteInt(name);
    }

    @Override
    public void beginTask(String taskName, int workTotal)
    {
        setNoteInt(taskName);
        setProgress(workTotal);
    }

    @Override
    public void internalWorked(double work)
    {
        progress += work;
        setProgressInt(progress);
    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor)
    {
        return this;
    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor, String title, int progress)
    {
        setProgress(progress);
        setNoteInt(title);
        return this;
    }

    @Override
    public void worked(int worked)
    {
        setProgressInt(worked);
    }

    @Override
    public void setCanceled(boolean cancel)
    {
        this.isCanceled = cancel;
        if (cancel)
        {
            done();
        }
    }

    /**
     * Stars progress bar autoincrement in separate thread
     *
     */
    public void startAutoincrement()
    {
        new Thread(new AutoIncrement()).start();
    }


    /**
     * Autoincrement class to keeps progress bar updated even when there are no signal from process
     */
    private class AutoIncrement implements Runnable
    {
        public static final int SLEEP_TIME = 200;

        @Override
        public void run()
        {
            while (isRunning())
            {

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        int newValue = progressBar.getValue();
                        newValue += 1;

                        if (newValue == MAX_VALUE)
                        {
                            newValue = (MAX_VALUE - progress) / 2;
                        }

                        setProgressInt(newValue);

                    }
                });


                try
                {
                    Thread.sleep(SLEEP_TIME);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
