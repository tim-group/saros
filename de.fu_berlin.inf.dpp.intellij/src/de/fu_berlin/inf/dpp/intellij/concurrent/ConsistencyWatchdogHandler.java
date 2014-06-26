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
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.Status;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.runtime.UIMonitoredJob;
import de.fu_berlin.inf.dpp.session.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;


/**
 * This component is responsible for handling Consistency Errors on the host
 */
public class ConsistencyWatchdogHandler extends AbstractActivityProvider
        implements Startable {

    private static Logger LOG = Logger.getLogger(ConsistencyWatchdogHandler.class);

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

        LOG.debug("Received Checksum Error: " + checksumError);


        UIMonitoredJob recoveryJob = new UIMonitoredJob("File recovery")
        {
            @Override
            protected IStatus run(IProgressMonitor monitor)
            {
               runRecovery(checksumError, monitor.convert());

               return new Status(IStatus.OK);
            }
        };

       recoveryJob.schedule();

    }

    protected void runRecovery(ChecksumErrorActivity checksumError,
                               IProgressMonitor progress) throws CancellationException {

        List<StartHandle> startHandles = null;

        progress.beginTask("Performing recovery", 1200);
        try {

            startHandles = sarosSession.getStopManager().stop(
                    sarosSession.getUsers(), "Consistency recovery");

            progress.subTask("Sending files to client...");
            recoverFiles(checksumError, progress);

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
                LOG.error("could not find start handle"
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
                                IProgressMonitor progress) {

        progress.beginTask("Sending files", checksumError.getPaths().size() + 1);

        try {
            for (SPath path : checksumError.getPaths()) {
                progress.subTask("Recovering file: "
                        + path.getProjectRelativePath());
                recoverFile(checksumError.getSource(), sarosSession, path,
                        progress);
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
                               final SPath path, IProgressMonitor progress) {

        progress.beginTask("Handling file: " + path.toString(), 10);

        IFile file = path.getFile();

        // Save document before sending to client
        if (file.exists()) {
            try {
                editorManager.saveLazy(path);
            } catch (FileNotFoundException e) {
                LOG.error("File could not be found, despite existing: " + path,
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
                LOG.error("File could not be read, despite existing: " + path,
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
