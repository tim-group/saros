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

package de.fu_berlin.inf.dpp.intellij.feedback;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.feedback.AbstractFeedbackManager;
import de.fu_berlin.inf.dpp.core.feedback.FeedbackPreferences;
import de.fu_berlin.inf.dpp.core.feedback.StatisticManagerConfiguration;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.Status;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.Job;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.annotations.Component;

import de.fu_berlin.inf.dpp.session.ISarosSession;


/**
 * The ErrorLogManager is supposed to upload an error log file to our server at
 * the end of a session, if the user gave his permission to this. The error log
 * contains all errors and warnings per default. If the user furthermore allowed
 * the submission of the full log (i.e. with debug and info messages) via the
 * {@link FeedbackPreferencePage}, then the full log is submitted.
 *
 * @author Lisa Dohrmann
 *
 * @TODO provide a better way to access the preference constants. coezbek wants
 *       to use his preference framework for this
 *
 */
@Component(module = "feedback")
public class ErrorLogManager extends AbstractFeedbackManager implements
        Startable {

    private static final Logger log = Logger.getLogger(ErrorLogManager.class
            .getName());

    protected final String sessionID;
    private static String mostRecentSessionID;

    private static synchronized void rememberSession(String sessionId) {
        mostRecentSessionID = sessionId;
    }

    @Override
    public void start() {
        // save the id of the just started session
        rememberSession(sessionID);
    }

    @Override
    public void stop() {
        if (isErrorLogSubmissionAllowed())
            submitErrorLog();
    }

    public ErrorLogManager(final ISarosSession session) {
        sessionID = session.getID();

        logErrorLogSettings();
    }

    /**
     * Returns if the submission of the error log is allowed as a boolean.
     *
     * @return true if it is allowed
     */
    public static boolean isErrorLogSubmissionAllowed() {
        return getErrorLogSubmissionStatus() == ALLOW;
    }

    /**
     * Returns whether the submission of the error log is allowed, forbidden or
     * unknown.
     *
     * @return 0 = unknown, 1 = allowed, 2 = forbidden
     */
    public static int getErrorLogSubmissionStatus() {

        return FeedbackPreferences.getPreferences().getInt(
                PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION, UNDEFINED);
    }

    /**
     * Sets if the user wants to submit the error log.
     */
    public static void setErrorLogSubmissionAllowed(final boolean allowed) {
        final int submission = allowed ? ALLOW : FORBID;
        FeedbackPreferences.getPreferences().putInt(
                PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION, submission);
    }

    /**
     * Returns if the user gave an answer to the error log submission.
     *
     * @return true if the user either allowed or forbade the submission,
     *         otherwise false
     */
    public static boolean hasErrorLogAgreement() {
        return getErrorLogSubmissionStatus() != UNKNOWN;
    }

    /**
     * Returns if the submission of the full error log is allowed as a boolean.
     *
     * @return true if it is allowed
     */
    public static boolean isFullErrorLogSubmissionAllowed() {
        return getFullErrorLogSubmissionStatus() == ALLOW;
    }

    /**
     * Returns whether the submission of the *full* error log, i.e. the log that
     * contains errors and warnings as well as debug and info messages, is
     * allowed or forbidden
     *
     * @return ALLOW or FORBID
     */
    public static int getFullErrorLogSubmissionStatus() {
        return FeedbackPreferences.getPreferences().getInt(
                PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL, UNDEFINED);
    }

    /**
     * Sets if the user wants to submit the *full* error log.
     */
    public static void setFullErrorLogSubmissionAllowed(final boolean allowed) {
        final int submission = allowed ? ALLOW : FORBID;
        FeedbackPreferences.getPreferences().putInt(
                PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL, submission);
    }

    /**
     * Returns the location of the error log for the given appender. The
     * location is determined by iterating through all log appenders until a
     * FileAppender with the given appenderName is found. Its current value of
     * the File option is than returned as the path of the error log.
     *
     * @return the location of the error log file or null if none could be
     *         determined
     */
    public static String getErrorLogLocation(String appenderName) {
        Enumeration<?> appenders = Logger.getRootLogger().getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender app = (Appender) appenders.nextElement();
            if (app instanceof FileAppender) {
                // find a FileAppender with the given Name
                if (app.getName().equals(appenderName))
                    return ((FileAppender) app).getFile();
            }
        }
        return null;
    }

    /**
     * Returns the error log file produced by the ErrosOnlyAppender or null, if
     * none could be found.
     *
     * @return an existing error log file or null
     */
    public static File getErrorsOnlyLogFile() {
        String errorLogLocation = getErrorLogLocation("ErrorsOnlyAppender");
        File file = new File(errorLogLocation);

        if (!file.exists()) {
            log.debug("There was no error log at " + errorLogLocation);
            return null;
        }
        return file;
    }

    /**
     * Returns the error log file produced by the SessionLogAppender or null, if
     * none could be found.
     *
     * @return an existing error log file or null
     */
    public static File getFullErrorLogFile() {
        String errorLogLocation = getErrorLogLocation("SessionLogAppender");
        File file = new File(errorLogLocation);

        if (!file.exists()) {
            log.debug("There was no error log at " + errorLogLocation);
            return null;
        }
        return file;
    }

    /**
     * Submits an error log (errors-only or full, depending on the user's
     * choice) to our server by wrapping the submission in an
     * IRunnableWithProgress to report feedback of the execution in the active
     * workbench windows status bar.
     *
     * @see FileSubmitter#uploadErrorLog(String, String, File, SubMonitor)
     *
     * @nonblocking
     * @cancelable
     */
    public static void submitErrorLog() {
        // determine which log should be uploaded
        final File errorLog = isFullErrorLogSubmissionAllowed() ? getFullErrorLogFile()
                : getErrorsOnlyLogFile();

        if (errorLog == null)
            return; // If there is no error-log we cannot sent one

        /*
         * cut off a possible file extension and extend the log name to make it
         * unique, e.g. with the userID and sessionID
         */
        final String logNameExtended = FilenameUtils.getBaseName(errorLog
                .getName())
                + "_"
                + StatisticManagerConfiguration.getUserID()
                + "_"
                + mostRecentSessionID;

        new Job("Uploading Error Log...") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    FileSubmitter.uploadErrorLog(
                            System.getProperty("java.io.tmpdir"), logNameExtended,
                            errorLog, monitor);
                } catch (IOException e) {
                    String msg = String.format("Couldn't upload file: %s. %s",
                            e.getMessage(), e.getCause() != null ? e.getCause()
                                    .getMessage() : "");
                    log.error(msg);
                    return new Status(IStatus.ERROR, Saros.SAROS, msg, e);
                } catch (OperationCanceledException e) {
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }

        }.schedule();
    }

    protected void logErrorLogSettings() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current error log settings:\n");
        sb.append("  Error log submission allowed: "
                + isErrorLogSubmissionAllowed() + "\n");
        sb.append("  Full Error log submission allowed: "
                + isFullErrorLogSubmissionAllowed());
        log.info(sb.toString());
    }
}
