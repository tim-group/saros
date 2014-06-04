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

/**
 * Saros progress bar with autoincrement to use in UI
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-25
 * Time: 13:13
 */

public class SarosProgressBar implements IProgressMonitor
{
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 100;

    protected int progress;
    protected String info;

    protected DisplayContainer display;

    protected boolean isCanceled = false;
    private boolean isDone = false;

    private FinishListener finishListener;

    /**
     * Do not use this outside package!
     */
    protected SarosProgressBar()
    {
    }

    /**
     * Do not use this outside package!
     */
    protected SarosProgressBar(DisplayContainer display)
    {
        this.display = display;
    }

    /**
     * Creates progress bar w/o additional information
     *
     * @param progressBar
     */
    public SarosProgressBar(JProgressBar progressBar)
    {
        this(progressBar, null);
    }

    /**
     * Creates progress bar with additional information
     *
     * @param progressBar JProgressBar - progress information
     * @param infoLabel   JLabel - additional information
     */
    public SarosProgressBar(JProgressBar progressBar, JLabel infoLabel)
    {
        this.display = new DisplayContainer(progressBar, infoLabel);
    }

    /**
     * Sets real progress to UI
     *
     * @param progress progress
     */
    public void setProgress(int progress)
    {
        this.progress = progress;
        this.display.setProgress(progress);
    }

    /**
     * Checks progress bar state
     *
     * @return boolean
     */
    private boolean isRunning()
    {
        return isCanceled && !isDone && progress < MAX_VALUE;
    }

    @Override
    public boolean isCanceled()
    {
        return isCanceled;
    }

    @Override
    public void setCanceled(boolean cancel)
    {
        this.isCanceled = cancel;
        done();
    }

    @Override
    public void worked(int delta)
    {
        setProgress(this.progress + delta);
    }


    @Override
    public void subTask(String remaingTime)
    {
        setTaskName(remaingTime);
    }

    @Override
    public void setTaskName(String name)
    {
        this.display.setInfo(name);
        this.info = name;
    }

    @Override
    public void done()
    {
        setProgress(MAX_VALUE);
        isDone = true;
        if (finishListener != null)
        {
            finishListener.finished();
        }
    }

    @Override
    public void beginTask(String taskName, String type)
    {
        this.display.setInfo(taskName);
        this.info = taskName;

    }

    @Override
    public void beginTask(String taskName, int progress)
    {
        setProgress(progress);
        this.display.setInfo(taskName);
        this.info=taskName;

    }

    @Override
    public void internalWorked(double work)
    {
        worked((int) work);
    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor)
    {
        return new SubProgressBar(this);
    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor, String title, int progress)
    {
        setProgress(progress);
        this.display.setInfo(title);
        this.info = title;

        return new SubProgressBar(this);
    }



    /**
     * @param finishListener FinishListener
     */
    public void setFinishListener(FinishListener finishListener)
    {
        this.finishListener = finishListener;
    }

    /**
     * Stars progress bar autoincrement in separate thread
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
                        int newValue = display.getProgressBar().getValue();
                        newValue += 1;

                        if (newValue == MAX_VALUE)
                        {
                            newValue = (MAX_VALUE - progress) / 2;
                        }

                        display.setProgress(newValue);

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

    /**
     * Interface creates structure to listen progress bar events
     */
    public interface FinishListener
    {
        /**
         * Fires when progress monitor is finished
         */
        void finished();
    }

    protected class DisplayContainer
    {
        private JProgressBar progressBar;
        private JLabel infoLabel;

        public DisplayContainer(JProgressBar progressBar, JLabel infoLabel)
        {
            this.progressBar = progressBar;
            this.infoLabel = infoLabel;

            this.progressBar.setEnabled(true);
            this.progressBar.setMinimum(MIN_VALUE);
            this.progressBar.setMaximum(MAX_VALUE);

            if (infoLabel == null)
            {
                this.progressBar.setStringPainted(true);
            }

            this.progressBar.setVisible(true);
            this.infoLabel.setVisible(true);
        }

        public JProgressBar getProgressBar()
        {
            return progressBar;
        }

        public JLabel getInfoLabel()
        {
            return infoLabel;
        }

        /**
         * Sets value to UI
         *
         * @param progress
         */
        protected void setProgress(final int progress)
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
         * Sets info to UI
         *
         * @param info additional progress information
         */
        protected void setInfo(final String info)
        {
            if(info==null)
                return;

            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    if (infoLabel == null)
                    {
                        progressBar.setString(info);
                    }
                    else
                    {
                        infoLabel.setText(info);
                    }
                }
            });

        }

        protected void reset()
        {
            setProgress(0);
            setInfo("");
        }
    }

}
