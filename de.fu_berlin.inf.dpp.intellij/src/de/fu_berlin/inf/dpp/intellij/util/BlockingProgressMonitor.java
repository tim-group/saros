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

package de.fu_berlin.inf.dpp.intellij.util;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * A ProgressMonitor which can be waited upon. </p> <s> Typical usage pattern:
 * <p/>
 * <p/>
 * <p/>
 * <pre>
 * BlockingProgressMonitor blockingMonitor = new BlockingProgressMonitor(monitor);
 * object.longRunningOperation(blockingMonitor);
 *
 * try {
 *     blockingMonitor.await();
 * } catch (InterruptedException e) {
 *     log.error(&quot;Code not designed to be interruptible&quot;, e);
 *     Thread.currentThread().interrupt();
 * }
 * </pre>
 * <p/>
 * </s>
 * <p/>
 * Please use only the {@link #await(long)} method of that class.
 *
 * @deprecated
 */

@Deprecated
public class BlockingProgressMonitor implements IProgressMonitor//extends ProgressMonitorWrapper
{

    protected CountDownLatch latch;

    public BlockingProgressMonitor()
    {
        this(new NullProgressMonitor());
    }

    /**
     * Will delegate all calls to the given ProgressMonitor (can still maintain
     * the possibility to wait on the completion of the task).
     */
    public BlockingProgressMonitor(IProgressMonitor delegate)
    {
        //super(delegate);
        this.latch = new CountDownLatch(1);
    }

   // @Override
    public void done()
    {
        //super.done();
        this.latch.countDown();
    }

    @Override
    public void beginTask(String taskName, String type)
    {

    }

    @Override
    public void beginTask(String taskNam, int size)
    {

    }

    @Override
    public void internalWorked(double work)
    {

    }

    @Override
    public ISubMonitor convert()
    {
        return null;
    }

    @Override
    public ISubMonitor convert(String title, int progress)
    {
        return null;
    }

    @Override
    public boolean isCanceled()
    {
        return false;
    }

    // @Override
    public void setCanceled(boolean cancelled)
    {
      //  super.setCanceled(cancelled);
        if (cancelled)
        {
            this.latch.countDown();
        }
    }

    @Override
    public void worked(int delta)
    {

    }

    @Override
    public void subTask(String remaingTime)
    {

    }

    @Override
    public void setTaskName(String name)
    {

    }

    /**
     * @throws InterruptedException
     * @deprecated Can cause deadlocks or infinitive waits if the corresponding
     * logic fails to call {@link #done()} on the given monitor.
     */
    @Deprecated
    public void await() throws InterruptedException
    {
        this.latch.await();
    }

    public boolean await(long timeout) throws InterruptedException
    {
        return this.latch.await(timeout, TimeUnit.SECONDS);
    }
}
