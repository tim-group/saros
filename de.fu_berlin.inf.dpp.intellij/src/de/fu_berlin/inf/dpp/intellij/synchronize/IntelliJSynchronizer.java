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

package de.fu_berlin.inf.dpp.intellij.synchronize;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import de.fu_berlin.inf.dpp.intellij.mock.editor.PlatformUI;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;

public class IntelliJSynchronizer implements UISynchronizer {

    private static final Logger LOG = Logger.getLogger(IntelliJSynchronizer.class);

    @Override
    public void asyncExec(Runnable runnable) {
        exec(runnable, true);
    }

    @Override
    public void syncExec(Runnable runnable) {
        exec(runnable, false);
    }

    @Override
    public boolean isUIThread() {
        return ApplicationManager.getApplication().isDispatchThread();
    }

    private void exec(Runnable runnable, boolean async) {
        Application application = ApplicationManager.getApplication();
        try {

            if (async) {
                application.invokeLater(runnable);
            } else {
                application.invokeAndWait(runnable, ModalityState.NON_MODAL);
            }

        } catch (Exception e) {

            if (PlatformUI.getWorkbench().isClosing()) {
                LOG.warn("could not execute runnable " + runnable
                        + ", UI thread is not available", new StackTrace());
            } else {
                LOG.error("could not execute runnable " + runnable + ", workbench display was disposed before workbench shutdown", e);
            }
        }
    }
}
