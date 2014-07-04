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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.concurrent.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.concurrent.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.intellij.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationHandler;
import de.fu_berlin.inf.dpp.intellij.ui.views.toolbar.ConsistencyButton;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.MonitorProgressBar;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.ProgressFrame;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.observables.ValueChangeListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.picocontainer.annotations.Inject;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks shared project consistency
 */
public class ConsistencyAction extends AbstractSarosAction {
    public static final String ACTION_NAME = "consistency";


    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            setSharedProject(newSarosSession);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            setSharedProject(null);
        }
    };

    private final ValueChangeListener<Boolean> isConsistencyListener = new ValueChangeListener<Boolean>() {

        @Override
        public void setValue(Boolean newValue) {
            handleConsistencyChange(newValue);
        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;

    @Inject
    protected ConsistencyWatchdogClient watchdogClient;

    @Inject
    protected IsInconsistentObservable inconsistentObservable;


    private ConsistencyButton consistencyButton;
    private ISarosSession sarosSession;


    public ConsistencyAction() {
        SarosPluginContext.initComponent(this);

        setSharedProject(sessionManager.getSarosSession());
        sessionManager.addSarosSessionListener(sessionListener);
    }

    private void setSharedProject(ISarosSession newSharedProject) {

        // Unregister from previous project
        if (sarosSession != null) {
            inconsistentObservable.remove(isConsistencyListener);
        }

        sarosSession = newSharedProject;

        if (sarosSession != null) {
            inconsistentObservable.addAndNotify(isConsistencyListener);
        }

    }

    private void handleConsistencyChange(final Boolean isInconsistent) {

        if (sarosSession.isHost() && isInconsistent) {
            LOG.warn("No inconsistency should ever be reported" //$NON-NLS-1$
                    + " to the host"); //$NON-NLS-1$
            return;
        }
        LOG.debug("Inconsistency indicator goes: " //$NON-NLS-1$
                + (isInconsistent ? "on" : "off")); //$NON-NLS-1$ //$NON-NLS-2$

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                consistencyButton.setInconsistent(isInconsistent);
            }
        });


        if (!isInconsistent) {
            setToolTipText(Messages.ConsistencyAction_tooltip_no_inconsistency);
            return;
        }


        final Set<SPath> paths = new HashSet<SPath>(
                watchdogClient.getPathsWithWrongChecksums());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                StringBuilder sb = new StringBuilder();

                for (SPath path : paths) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }

                    sb.append(path.getFullPath().toOSString());
                }

                String files = sb.toString();

                // set tooltip
                setToolTipText(MessageFormat.format(
                        Messages.ConsistencyAction_tooltip_inconsistency_detected,
                        files));

                // TODO Balloon is too aggressive at the moment, when
                // the host is slow in sending changes (for instance
                // when refactoring)

                // show balloon notification
                NotificationHandler
                        .showNotification(
                                Messages.ConsistencyAction_title_inconsistency_deteced,
                                MessageFormat
                                        .format(
                                                Messages.ConsistencyAction_message_inconsistency_detected,
                                                files)
                        );
            }
        });
    }

    private void setToolTipText(String text) {
        NotificationHandler.showNotification(text, "Consistency warning");
    }

    @Override
    public void run() {
        LOG.debug("user activated CW recovery.");

        final Set<SPath> paths = new HashSet<SPath>(
                watchdogClient.getPathsWithWrongChecksums());

        StringBuilder sbInconsistentFiles = new StringBuilder();
        for (SPath path : paths) {
            sbInconsistentFiles.append("project: ");
            sbInconsistentFiles.append(path.getProject().getName());
            sbInconsistentFiles.append(", file: ");
            sbInconsistentFiles.append(path.getProjectRelativePath().toOSString());
            sbInconsistentFiles.append("\n");

        }

        sbInconsistentFiles.append("\nWould you like to get last changes?\n");


        if (!DialogUtils.showQuestion(guiFrame, sbInconsistentFiles.toString(), Messages.ConsistencyAction_confirm_dialog_title)) {
            consistencyButton.setEnabled(true);

            return;
        }


        final ProgressFrame progress = new ProgressFrame("Consistency action");
        progress.setFinishListener(new MonitorProgressBar.FinishListener() {
            @Override
            public void finished() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        consistencyButton.setEnabled(true);
                        consistencyButton.setInconsistent(watchdogClient.getPathsWithWrongChecksums().size() > 0);
                    }
                });


            }
        });

        ThreadUtils.runSafeAsync(LOG, new Runnable() {
            @Override
            public void run() {

                progress.beginTask(
                        Messages.ConsistencyAction_progress_perform_recovery, IProgressMonitor.UNKNOWN);
                watchdogClient.runRecovery(progress.convert());

                //  progress.done();
            }
        });
    }

    public void setConsistencyButton(ConsistencyButton consistencyButton) {
        this.consistencyButton = consistencyButton;
    }
}
