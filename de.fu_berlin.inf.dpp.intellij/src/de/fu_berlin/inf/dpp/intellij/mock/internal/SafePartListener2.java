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

package de.fu_berlin.inf.dpp.intellij.mock.internal;

import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IWorkbenchPartReference;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IPartListener2;
import org.apache.log4j.Logger;


import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * A listener which forwards calls to a another IPartListener2, but catches all
 * exception which might have occur in the forwarded to IPartListener2 and
 * prints them to the log given in the constructor.
 *
 * @pattern Proxy which adds the aspect of "safety"
 */
public class SafePartListener2 implements IPartListener2
{

    /**
     * The {@link IPartListener2} to forward all call to which are received by
     * this {@link IPartListener2}
     */
    protected IPartListener2 toForwardTo;

    /**
     * The {@link Logger} to use for printing an error message when a
     * RuntimeException occurs when calling the {@link #toForwardTo}
     * {@link IPartListener2}.
     */
    protected Logger log;

    public SafePartListener2(Logger log, IPartListener2 toForwardTo) {
        this.toForwardTo = toForwardTo;
        this.log = log;
    }

    @Override
    public void partActivated(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partActivated(partRef);
            }
        });
    }

    @Override
    public void partBroughtToTop(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partBroughtToTop(partRef);
            }
        });
    }

    @Override
    public void partClosed(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partClosed(partRef);
            }
        });
    }

    @Override
    public void partDeactivated(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partDeactivated(partRef);
            }
        });
    }

    @Override
    public void partHidden(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partHidden(partRef);
            }
        });
    }

    @Override
    public void partInputChanged(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partInputChanged(partRef);
            }
        });
    }

    @Override
    public void partOpened(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partOpened(partRef);
            }
        });
    }

    @Override
    public void partVisible(final IWorkbenchPartReference partRef) {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                toForwardTo.partVisible(partRef);
            }
        });
    }

}
