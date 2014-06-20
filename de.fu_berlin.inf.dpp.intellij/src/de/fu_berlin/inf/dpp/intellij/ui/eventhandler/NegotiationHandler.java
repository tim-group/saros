/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie UniversitÃ¤t Berlin - Fachbereich Mathematik und Informatik - 2010
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

package de.fu_berlin.inf.dpp.intellij.ui.eventhandler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


import de.fu_berlin.inf.dpp.communication.extensions.SarosSessionPacketExtension;
import de.fu_berlin.inf.dpp.core.invitation.*;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.Status;
import de.fu_berlin.inf.dpp.core.invitation.INegotiationHandler;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.intellij.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.intellij.ui.ISarosView;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.runtime.Job;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.*;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.ProgressMonitorAdapterFactory;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.AddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.SessionNegotiation;

import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;


/**
 * This handler is responsible for presenting and running the session and
 * project negotiations that are received by the Saros Session Manager
 * component.
 *
 * @author srossbach
 */
public class NegotiationHandler implements INegotiationHandler {

    public static final String NAMESPACE = SarosSessionPacketExtension.EXTENSION_NAMESPACE;
    private static final Logger LOG = Logger
            .getLogger(NegotiationHandler.class);

    private ISarosView sarosView = new SarosView();

    /**
     *
     * OutgoingInvitationJob wraps the instance of
     * {@link OutgoingSessionNegotiation} and cares about handling the
     * exceptions like local or remote cancellation.
     *
     * It notifies the user about the progress using the Eclipse Jobs API and
     * interrupts the process if the session closes.
     *
     */
    private class OutgoingInvitationJob extends Job {

        private OutgoingSessionNegotiation process;
        private String peer;

        public OutgoingInvitationJob(OutgoingSessionNegotiation process) {
            super(MessageFormat.format(
                    Messages.NegotiationHandler_inviting_user,
                    getNickname(process.getPeer())));
            this.process = process;
            this.peer = process.getPeer().getBase();

            setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                    ImageManager
                            .getImageDescriptor("/icons/elcl16/project_share_tsk.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                SessionNegotiation.Status status = process
                        .start(ProgressMonitorAdapterFactory.convertTo(monitor));

                switch (status) {
                    case CANCEL:
                        return Status.CANCEL_STATUS;
                    case ERROR:
                        return new Status(IStatus.ERROR, NAMESPACE,
                                process.getErrorMessage());
                    case OK:
                        break;
                    case REMOTE_CANCEL:
                        sarosView
                                .showNotification(
                                        Messages.NegotiationHandler_canceled_invitation,
                                        MessageFormat
                                                .format(
                                                        Messages.NegotiationHandler_canceled_invitation_text,
                                                        peer)
                                );

                        return new Status(
                                IStatus.CANCEL,
                                NAMESPACE,
                                MessageFormat
                                        .format(
                                                Messages.NegotiationHandler_canceled_invitation_text,
                                                peer));

                    case REMOTE_ERROR:
                        sarosView
                                .showNotification(
                                        Messages.NegotiationHandler_error_during_invitation,
                                        MessageFormat
                                                .format(
                                                        Messages.NegotiationHandler_error_during_invitation_text,
                                                        peer, process.getErrorMessage()));

                        return new Status(
                                IStatus.ERROR,
                                NAMESPACE,
                                MessageFormat
                                        .format(
                                                Messages.NegotiationHandler_error_during_invitation_text,
                                                peer, process.getErrorMessage()));
                }
            } catch (Exception e) {
                LOG.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, NAMESPACE, e.getMessage(), e);

            }

            sessionManager.startSharingProjects(process.getPeer());

            return Status.OK_STATUS;
        }
    }

    private class OutgoingProjectJob extends Job {

        private OutgoingProjectNegotiation process;
        private String peer;

        public OutgoingProjectJob(
                OutgoingProjectNegotiation outgoingProjectNegotiation) {
            super(Messages.NegotiationHandler_sharing_project);
            process = outgoingProjectNegotiation;
            peer = process.getPeer().getBase();

            setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                    ImageManager.getImageDescriptor("/icons/invites.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                ProjectNegotiation.Status status = process.start(monitor);
                String peerName = getNickname(new JID(peer));

                final String message;

                switch (status) {
                    case CANCEL:
                        return Status.CANCEL_STATUS;
                    case ERROR:
                        return new Status(IStatus.ERROR, NAMESPACE,
                                process.getErrorMessage());
                    case OK:
                        break;
                    case REMOTE_CANCEL:
                        message = MessageFormat
                                .format(
                                        Messages.NegotiationHandler_project_sharing_cancelled_text,
                                        peerName);

                        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.openInformationMessageDialog(
                                        SWTUtils.getShell(), message, message);
                            }
                        });

                        return new Status(IStatus.CANCEL, NAMESPACE, message);

                    case REMOTE_ERROR:
                        message = MessageFormat
                                .format(
                                        Messages.NegotiationHandler_sharing_project_cancelled_remotely,
                                        peerName, process.getErrorMessage());
                        sarosView
                                .showNotification(
                                        Messages.NegotiationHandler_sharing_project_cancelled_remotely_text,
                                        message);

                        return new Status(IStatus.ERROR, NAMESPACE, message);
                }
            } catch (Exception e) {
                LOG.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, NAMESPACE, e.getMessage(), e);

            }

            return Status.OK_STATUS;
        }
    }

    private final SarosUI sarosUI;

    private final ISarosSessionManager sessionManager;

    public NegotiationHandler(ISarosSessionManager sessionManager,
                              XMPPConnectionService connectionService, SarosUI sarosUI) {
        this.sarosUI = sarosUI;
        this.sessionManager = sessionManager;
        this.sessionManager.setNegotiationHandler(this);
    }

    @Override
    public void handleOutgoingSessionNegotiation(
            OutgoingSessionNegotiation negotiation) {

        OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(
                negotiation);

        outgoingInvitationJob.setPriority(Job.SHORT);
        outgoingInvitationJob.schedule();
    }

    @Override
    public void handleIncomingSessionNegotiation(
            IncomingSessionNegotiation negotiation) {
        showIncomingInvitationUI(negotiation);
    }

    @Override
    public void handleOutgoingProjectNegotiation(
            OutgoingProjectNegotiation negotiation) {

        OutgoingProjectJob job = new OutgoingProjectJob(negotiation);
        job.setPriority(Job.SHORT);
        job.schedule();
    }

    @Override
    public void handleIncomingProjectNegotiation(
            IncomingProjectNegotiation negotiation) {
        showIncomingProjectUI(negotiation);
    }

    private void showIncomingInvitationUI(
            final IncomingSessionNegotiation process)
    {

        // Fixes #2727848: InvitationDialog is opened in the
        // background
        ThreadUtils.runSafeAsync(LOG, new Runnable()
        {
            @Override
            public void run()
            {

                /**
                 * @JTourBusStop 8, Invitation Process:
                 *
                 *               (4a) The SessionManager then hands over the
                 *               control to the NegotiationHandler (this class)
                 *               which works on a newly started
                 *               IncomingSessionNegotiation. This handler opens
                 *               the JoinSessionWizard, a dialog for the user to
                 *               decide whether to next the invitation.
                 */


                JoinSessionWizard sessionWizard = new JoinSessionWizard(process);
            }
        });

    }

    private void showIncomingProjectUI(final IncomingProjectNegotiation process) {

        List<ProjectNegotiationData> pInfos = process.getProjectInfos();
        final List<FileList> fileLists = new ArrayList<FileList>(pInfos.size());

        for (ProjectNegotiationData pInfo : pInfos)
        {
            fileLists.add(pInfo.getFileList());
        }

        ThreadUtils.runSafeSync(LOG, new Runnable() {
            @Override
            public void run() {
                // AddProjectsDialogUI projectWizard = new AddProjectsDialog(process,  fileLists);
                AddProjectToSessionWizard projectToSessionWizard = new AddProjectToSessionWizard(process, process.getPeer(), fileLists, process
                        .getProjectNames());
            }
        });
    }

    private static String getNickname(JID jid) {
        String nickname = XMPPUtils.getNickname(null, jid);

        if (nickname == null)
            nickname = jid.getBareJID().toString();

        return nickname;
    }
}
