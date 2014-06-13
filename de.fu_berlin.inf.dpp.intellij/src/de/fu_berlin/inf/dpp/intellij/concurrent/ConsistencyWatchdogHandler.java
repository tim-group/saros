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

package de.fu_berlin.inf.dpp.intellij.concurrent;

import de.fu_berlin.inf.dpp.activities.*;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.core.misc.IRunnableWithProgress;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.ProgressMonitorDialog;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.session.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CancellationException;


/**
 * This component is responsible for handling Consistency Errors on the host
 */
@Component(module = "consistency")
public class ConsistencyWatchdogHandler extends AbstractActivityProvider
        implements Startable {

    private static Logger log = Logger
            .getLogger(ConsistencyWatchdogHandler.class);

    protected final EditorManager editorManager;

    protected final ConsistencyWatchdogClient watchdogClient;

    protected final ISarosSession sarosSession;

    protected final IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(ChecksumErrorActivity checksumError) {
            startRecovery(checksumError);
        }
    };

    @Override
    public void start() {
        installProvider(sarosSession);
    }

    @Override
    public void stop() {
        uninstallProvider(sarosSession);
    }

    public ConsistencyWatchdogHandler(ISarosSession sarosSession,
                                      EditorManager editorManager, ConsistencyWatchdogClient watchdogClient) {
        this.sarosSession = sarosSession;
        this.editorManager = editorManager;
        this.watchdogClient = watchdogClient;
    }

    @Override
    public void exec(IActivity activity) {
        if (!sarosSession.isHost())
            return;
        activity.dispatch(activityReceiver);
    }

    /**
     * This method creates and opens an error message which informs the user
     * that inconsistencies are handled and he should wait until the
     * inconsistencies are resolved. The Message are saved in a HashMap with a
     * pair of JID of the user and a string representation of the paths of the
     * handled files as key. You can use <code>closeChecksumErrorMessage</code>
     * with the same arguments to close this message again.
     */
    protected void startRecovery(final ChecksumErrorActivity checksumError) {

        log.debug("Received Checksum Error: " + checksumError);

        // execute async so outstanding activities could be dispatched
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {

                final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                        SWTUtils.getShell()) {
                    @Override
                    protected Image getImage() {
                        return getWarningImage();
                    }

                    // TODO add some text
                    // "Inconsitent file state has detected. File "
                    // + pathes
                    // + " from user "
                    // + from.getBase()
                    // +
                    // " has to be synchronized with project host. Please wait until the inconsistencies are resolved."
                };

                try {
                    /*
                     * run in a modal context otherwise we would block again the
                     * dispatching of activities
                     */
                    dialog.run(true, true, new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor) {
                            runRecovery(checksumError,
                                    monitor.convert());
                        }
                    });
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getCause();
                    } catch (CancellationException c) {
                        log.info("Recovery was cancelled by local user");
                    } catch (Throwable t) {
                        log.error("Internal Error: ", t);
                    }
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interruptable", e);
                }
            }
        });
    }

    protected void runRecovery(ChecksumErrorActivity checksumError,
                               ISubMonitor progress) throws CancellationException {

        List<StartHandle> startHandles = null;

        progress.beginTask("Performing recovery", 1200);
        try {

            startHandles = sarosSession.getStopManager().stop(
                    sarosSession.getUsers(), "Consistency recovery");

            progress.subTask("Sending files to client...");
            recoverFiles(checksumError, progress.newChild(900));

            /*
             * We have to start the StartHandle of the inconsistent user first
             * (blocking!) because otherwise the other participants can be
             * started before the inconsistent user completely processed the
             * consistency recovery.
             */
            progress.subTask("Wait for peers...");

            // find the StartHandle of the inconsistent user
            StartHandle inconsistentStartHandle = null;
            for (StartHandle startHandle : startHandles) {
                if (checksumError.getSource().equals(startHandle.getUser())) {
                    inconsistentStartHandle = startHandle;
                    break;
                }
            }
            if (inconsistentStartHandle == null) {
                log.error("could not find start handle"
                        + " of the inconsistent user");
            } else {
                // FIXME evaluate the return value
                inconsistentStartHandle.startAndAwait();
                startHandles.remove(inconsistentStartHandle);
            }
        } finally {
            if (startHandles != null)
                for (StartHandle startHandle : startHandles)
                    startHandle.start();
            progress.done();
        }
    }

    /**
     * @host This is only called on the host
     * @nonSWT This method should not be called from the SWT Thread!
     */
    protected void recoverFiles(ChecksumErrorActivity checksumError,
                                ISubMonitor progress) {

        progress
                .beginTask("Sending files", checksumError.getPaths().size() + 1);

        try {
            for (SPath path : checksumError.getPaths()) {
                progress.subTask("Recovering file: "
                        + path.getProjectRelativePath());
                recoverFile(checksumError.getSource(), sarosSession, path,
                        progress.newChild(1));
            }

            // Tell the user that we sent all files
            fireActivity(new ChecksumErrorActivity(sarosSession.getLocalUser(),
                    checksumError.getSource(), null, checksumError.getRecoveryID()));
        } finally {
            progress.done();
        }
    }

    /**
     * Recover a single file for the given user (that is either send the file or
     * tell the user to removeAll it).
     */
    protected void recoverFile(User from, final ISarosSession sarosSession,
                               final SPath path, ISubMonitor progress) {

        progress.beginTask("Handling file: " + path.toString(), 10);

        IFile file = path.getFile();

        // Save document before sending to client
        if (file.exists()) {
            try {
                editorManager.saveLazy(path);
            } catch (FileNotFoundException e) {
                log.error("File could not be found, despite existing: " + path,
                        e);
            }
        }
        progress.worked(1);

        // Reset jupiter
        sarosSession.getConcurrentDocumentServer().reset(from, path);

        progress.worked(15);
        final User user = sarosSession.getLocalUser();

        if (file.exists()) {

            try {

                byte[] content = FileUtils.getLocalFileContent(file);

                if (content == null) {
                    throw new IOException();
                }

                // Send the file to client
                fireActivity(RecoveryFileActivity.created(
                        user, path, content, from));

                String checksum = new String(content);

                fireActivity(new ChecksumActivity(user,
                        path, checksum.hashCode(), checksum.length(), null));
            } catch (IOException e) {
                log.error("File could not be read, despite existing: " + path,
                        e);
            }
        } else {
            // TODO Warn the user...
            // Tell the client to delete the file
            fireActivity(RecoveryFileActivity.removed(user,
                    path, from));
            fireActivity(ChecksumActivity.missing(user, path));

            progress.worked(8);
        }
        progress.done();
    }


    protected Image getWarningImage() {
        return null;

    }
}
