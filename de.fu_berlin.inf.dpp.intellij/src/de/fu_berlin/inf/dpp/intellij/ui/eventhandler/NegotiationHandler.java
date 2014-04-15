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

import de.fu_berlin.inf.dpp.core.invitation.*;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.core.monitor.Status;
import de.fu_berlin.inf.dpp.core.project.INegotiationHandler;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.*;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.AddProjectToSessionWizard;

import de.fu_berlin.inf.dpp.intellij.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.invitation.FileList;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This handler is responsible for presenting and running the session and
 * project negotiations that are received by the Saros Session Manager
 * component.
 *
 * @author srossbach
 */
public class NegotiationHandler implements INegotiationHandler
{
    private static final Logger log = Logger.getLogger(NegotiationHandler.class);

    /**
     * OutgoingInvitationJob wraps the instance of
     * {@link OutgoingSessionNegotiation} and cares about handling the
     * exceptions like local or remote cancellation.
     * <p/>
     * It notifies the user about the progress using the Eclipse Jobs API and
     * interrupts the process if the session closes.
     */
    private class OutgoingInvitationJob extends Job
    {

        private OutgoingSessionNegotiation process;
        private String peer;

        public OutgoingInvitationJob(OutgoingSessionNegotiation process)
        {
            super(MessageFormat.format(
                    Messages.NegotiationHandler_inviting_user,
                    User.getHumanReadableName(connectionService, process.getPeer())));
            this.process = process;
            this.peer = process.getPeer().getBase();

            setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                    ImageManager
                            .getImageDescriptor("/icons/elcl16/project_share_tsk.png")
            );
        }

        @Override
        protected IStatus run(IProgressMonitor monitor)
        {
        try {
                SessionNegotiation.Status status = process.start(monitor);

                switch (status) {
                    case CANCEL:
                        return Status.CANCEL_STATUS;
                    case ERROR:
                        return new Status(IStatus.ERROR, Saros.SAROS,
                                process.getErrorMessage());
                    case OK:
                        break;
                    case REMOTE_CANCEL:
                        SarosView
                                .showNotification(
                                        Messages.NegotiationHandler_canceled_invitation,
                                        MessageFormat
                                                .format(
                                                        Messages.NegotiationHandler_canceled_invitation_text,
                                                        peer));

                        return new Status(
                                IStatus.CANCEL,
                                Saros.SAROS,
                                MessageFormat
                                        .format(
                                                Messages.NegotiationHandler_canceled_invitation_text,
                                                peer));

                    case REMOTE_ERROR:
                        SarosView
                                .showNotification(
                                        Messages.NegotiationHandler_error_during_invitation,
                                        MessageFormat
                                                .format(
                                                        Messages.NegotiationHandler_error_during_invitation_text,
                                                        peer, process.getErrorMessage()));

                        return new Status(
                                IStatus.ERROR,
                                Saros.SAROS,
                                MessageFormat
                                        .format(
                                                Messages.NegotiationHandler_error_during_invitation_text,
                                                peer, process.getErrorMessage()));
                }
            } catch (Exception e) {
                log.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);

            }

            sessionManager.startSharingProjects(process.getPeer());

            return Status.OK_STATUS;


        }
    }

    private class OutgoingProjectJob extends Job
    {

        private OutgoingProjectNegotiation process;
        private String peer;

        public OutgoingProjectJob(
                OutgoingProjectNegotiation outgoingProjectNegotiation)
        {
            super(Messages.NegotiationHandler_sharing_project);
            process = outgoingProjectNegotiation;
            peer = process.getPeer().getBase();

            setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                    ImageManager.getImageDescriptor("/icons/invites.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor)
        {
            try
            {
                ProjectNegotiation.Status status = process.start(monitor);
                String peerName = User.getHumanReadableName(connectionService, new JID(
                        peer));

                final String message;

                switch (status)
                {
                    case CANCEL:
                        return Status.CANCEL_STATUS;
                    case ERROR:
                        return new Status(IStatus.ERROR, Saros.SAROS, process.getErrorMessage());
                    case OK:
                        break;
                    case REMOTE_CANCEL:
                        message = MessageFormat
                                .format(
                                        Messages.NegotiationHandler_project_sharing_cancelled_text,
                                        peerName);

                        SWTUtils.runSafeSWTAsync(log, new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                DialogUtils.openInformationMessageDialog(
                                        SWTUtils.getShell(), message, message);
                            }
                        });

                        return new Status(IStatus.CANCEL, Saros.SAROS, message);

                    case REMOTE_ERROR:
                        message = MessageFormat
                                .format(
                                        Messages.NegotiationHandler_sharing_project_cancelled_remotely,
                                        peerName, process.getErrorMessage());
                        SarosView.showNotification(
                                Messages.NegotiationHandler_sharing_project_cancelled_remotely_text,
                                message);

                        return new Status(IStatus.ERROR, Saros.SAROS, message);
                }
            }
            catch (Exception e)
            {
                log.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);

            }

            return Status.OK_STATUS;
        }
    }


    private final ISarosSessionManager sessionManager;

    private final XMPPConnectionService connectionService;


    public NegotiationHandler(ISarosSessionManager sessionManager,
            XMPPConnectionService connectionService)
    {

        this.connectionService = connectionService;
        this.sessionManager = sessionManager;
        this.sessionManager.setNegotiationHandler(this);

    }

    @Override
    public void handleIncomingSessionNegotiation(
            IncomingSessionNegotiation negotiation)
    {

        showIncomingInvitationUI(negotiation);
    }

    private void showIncomingInvitationUI(
            final IncomingSessionNegotiation process)
    {


//
//        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
//            @Override
//            public void run() {
//                sarosUI.openSarosView();
//            }
//        });

        // Fixes #2727848: InvitationDialog is opened in the
        // background
        ThreadUtils.runSafeAsync(log, new Runnable()
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
                 *               decide whether to accept the invitation.
                 */


                JoinSessionWizard sessionWizard = new JoinSessionWizard(process);
                //JoinSessionDialog sessionWizard = new JoinSessionDialog(process);

                //todo
//
//                final WizardDialogAccessible wizardDialog = new WizardDialogAccessible(SWTUtils.getShell(), sessionWizard);
//
//                // TODO Provide help :-)
//                wizardDialog.setHelpAvailable(false);
//
//                // as we are not interested in the result
//                wizardDialog.setBlockOnOpen(false);
//
//                DialogUtils.openWindow(wizardDialog);
            }
        });

    }

    @Override
    public void handleOutgoingSessionNegotiation(
            OutgoingSessionNegotiation negotiation)
    {

        OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(
                negotiation);

        outgoingInvitationJob.setPriority(Job.SHORT);
        outgoingInvitationJob.schedule();
    }


    @Override
    public void handleOutgoingProjectNegotiation(
            OutgoingProjectNegotiation negotiation)
    {

        OutgoingProjectJob job = new OutgoingProjectJob(negotiation);
        job.setPriority(Job.SHORT);
        job.schedule();
    }

    @Override
    public void handleIncomingProjectNegotiation(
            IncomingProjectNegotiation negotiation)
    {
        showIncomingProjectUI(negotiation);
    }

    private void showIncomingProjectUI(final IncomingProjectNegotiation process)
    {
        //todo: UI implementation
        System.out.println("NegotiationHandler.showIncomingProjectUI //todo");


        List<ProjectNegotiationData> pInfos = process.getProjectInfos();
        final List<FileList> fileLists = new ArrayList<FileList>(pInfos.size());

        for (ProjectNegotiationData pInfo : pInfos)
        {
            fileLists.add(pInfo.getFileList());
        }

        ThreadUtils.runSafeSync(log, new Runnable()
        {

            @Override
            public void run()
            {
                AddProjectToSessionWizard projectWizard = new AddProjectToSessionWizard(
                        process, process.getPeer(), fileLists, process
                        .getProjectNames()
                );

                final WizardDialogAccessible wizardDialog = new WizardDialogAccessible(
                        SWTUtils.getShell(), projectWizard, SWT.MIN | SWT.MAX,
                        SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL
                                | SWT.PRIMARY_MODAL
                );

                /*
                 * IMPORTANT: as the dialog is non modal it MUST NOT block on
                 * open or there is a good chance to crash the whole GUI
                 *
                 * Scenario: A modal dialog is currently open with
                 * setBlockOnOpen(true) (as most input dialogs are).
                 *
                 * When we now open this wizard with setBlockOnOpen(true) this
                 * wizard will become the main dispatcher for the SWT Thread. As
                 * this wizard is non modal you cannot close it because you
                 * could not access it. Therefore the modal dialog cannot be
                 * closed as well because it is stuck on the non modal dialog
                 * which currently serves as main dispatcher !
                 */

                wizardDialog.setBlockOnOpen(false);

                wizardDialog.setHelpAvailable(false);
                projectWizard.setWizardDlg(wizardDialog);

                DialogUtils.openWindow(wizardDialog); //todo
            }
        });

    }

}
