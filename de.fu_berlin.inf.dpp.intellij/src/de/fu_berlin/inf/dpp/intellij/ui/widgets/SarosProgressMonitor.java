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

package de.fu_berlin.inf.dpp.intellij.ui.widgets;

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressSubMonitor;
import de.fu_berlin.inf.dpp.intellij.core.Saros;

import javax.swing.*;
import java.awt.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-25
 * Time: 13:13
 */

public class SarosProgressMonitor extends ProgressMonitor implements IProgressMonitor
{
    protected int progress;
    protected final String title;

    /**
     * Constructs a graphic object that shows progress, typically by filling
     * in a rectangular bar as the process nears completion.
     *
     * @param parentComponent the parent component for the dialog box
     * @param message         a descriptive message that will be shown
     *                        to the user to indicate what operation is being monitored.
     *                        This does not change as the operation progresses.
     *                        See the message parameters to methods in
     *                        {@link javax.swing.JOptionPane#message}
     *                        for the range of values.
     * @param note            a short note describing the state of the
     *                        operation.  As the operation progresses, you can call
     *                        setNote to change the note displayed.  This is used,
     *                        for example, in operations that iterate through a
     *                        list of files to show the name of the file being processes.
     *                        If note is initially null, there will be no note line
     *                        in the dialog box and setNote will be ineffective
     * @param min             the lower bound of the range
     * @param max             the upper bound of the range
     * @see javax.swing.JDialog
     * @see javax.swing.JOptionPane
     */
    public SarosProgressMonitor(Component parentComponent, Object message, String note, int min, int max)
    {
        super(parentComponent, message, note, min, max);
        this.title = message.toString();
    }

    public SarosProgressMonitor(String title)
    {
        super( new JFrame(title), title, "", 0, 100);
        this.title = title;

    }

    @Override
    public void setCanceled(boolean cancel)
    {
        super.close();
    }

    @Override
    public void worked(int delta)
    {
        this.progress += delta;
        super.setProgress(delta);
    }

    public void setProgress(int progress)
    {
        this.progress = progress;
        super.setProgress(progress);
    }

    @Override
    public void subTask(String remaingTime)
    {
        //todo
    }

    @Override
    public void setTaskName(final String name)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                setNote(name);
            }
        });


    }

    @Override
    public void done()
    {
        super.close();
    }

    @Override
    public void beginTask(String taskName, String type)
    {
        setTaskName(taskName);
        super.setProgress(progress);
    }

    @Override
    public void beginTask(String taskName, int size)
    {
        setTaskName(taskName);
        setProgress(size);
    }

    @Override
    public void internalWorked(double work)
    {

    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor)
    {
        return new SarosSubProgressMonitor(title,monitor);
    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor, String title, int progress)
    {
        return new SarosSubProgressMonitor(title,monitor);  //todo
    }
}
