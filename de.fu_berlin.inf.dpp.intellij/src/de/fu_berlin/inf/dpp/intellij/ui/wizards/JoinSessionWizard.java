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

package de.fu_berlin.inf.dpp.intellij.ui.wizards;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.27
 * Time: 08.51
 */

import de.fu_berlin.inf.dpp.core.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.ProcessTools;
import de.fu_berlin.inf.dpp.core.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.ui.IJoinSession;
import de.fu_berlin.inf.dpp.core.versioning.VersionManager;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import javax.swing.*;
import java.awt.*;


/**
 * A wizard that guides the user through an incoming invitation process.
 * <p/>
 * TODO Automatically switch to follow mode
 * <p/>
 * TODO Create a separate Wizard class with the following concerns implemented
 * more nicely: Long-Running Operation after each step, cancellation by a remote
 * party, auto-advance.
 *
 * @author rdjemili
 */
public class JoinSessionWizard extends AbstractWizard implements IJoinSession
{

    private static final Logger log = Logger.getLogger(JoinSessionWizard.class);

    private boolean accepted = false;

    private IncomingSessionNegotiation process;

    //  private ShowDescriptionPage descriptionPage;

    private SessionNegotiation.Status invitationStatus;

    @Inject
    private VersionManager manager;

    @Inject
    private PreferenceUtils preferenceUtils;

    public JoinSessionWizard(IncomingSessionNegotiation process)
    {
        //todo: UI wizard implementation
        System.out.println("JoinSessionWizard.JoinSessionWizard //todo");
        this.process = process;
        this.process.setInvitationUI(this);


        final IncomingSessionNegotiation proc = process;
        final Component comp = Saros.instance().getMainPanel();
        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {

                // Messages.showCheckboxOkCancelDialog(comp, "Do you want to join session?", "Incomming session");
                int n = JOptionPane.showConfirmDialog(
                        comp,
                        "Do you want to join session from [" + proc.getPeer().getName() + "] ?\n" + proc.getDescription(),
                        "Incoming session",
                        JOptionPane.YES_NO_OPTION);

                if (n == 0)
                {
                    proc.accept(new NullProgressMonitor());


                }
                else
                {
                    //clicked NO or closed dialog
                    proc.localCancel("Not accepted", ProcessTools.CancelOption.NOTIFY_PEER);
                }
            }
        });

        //  final Component comp = Saros.instance().getMainPanel();
        //  Messages.showInfoMessage(comp, "Do you want to join session?", "Join session");
        //this.setTitle("Join session wizard");


        // SarosPluginContext.initComponent(this);

        // EnterProjectNamePageUtils.setPreferenceUtils(preferenceUtils);


        // setWindowTitle(Messages.JoinSessionWizard_title);
        // setHelpAvailable(false);
        //  setNeedsProgressMonitor(true);

        // descriptionPage = new ShowDescriptionPage(manager, process);
        // addPage(descriptionPage);

        //  joinSession();
    }

    public void joinSession()
    {
        process.accept(new NullProgressMonitor());
    }

    /*  public void createPageControls(Composite pageContainer)
    {
        this.descriptionPage.createControl(pageContainer);

        if (getContainer() instanceof WizardDialogAccessible) {
            ((WizardDialogAccessible) getContainer()).setWizardButtonLabel(
                    IDialogConstants.FINISH_ID, Messages.JoinSessionWizard_accept);
        }
    }*/


    @Override
    public boolean performFinish()
    {

        accepted = true;

        try
        {
            // getContainer().run(true, false, new IRunnableWithProgress() {
            /* getContainer().run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    try {
                        invitationStatus = process.accept(monitor);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });*/

            ThreadUtils.runSafeAsync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    IProgressMonitor monitor = new NullProgressMonitor(); //todo
                    invitationStatus = process.accept(monitor);
                }
            });


        }
        catch (Exception e)
        {
            Throwable cause = e.getCause();

            if (cause == null)
            {
                cause = e;
            }

            asyncShowCancelMessage(process.getPeer(), e.getMessage(),
                    ProcessTools.CancelLocation.LOCAL);

            // give up, close the wizard as we cannot do anything here !
            return true;
        }

        switch (invitationStatus)
        {
            case OK:
                break;
            case CANCEL:
            case ERROR:
                asyncShowCancelMessage(process.getPeer(),
                        process.getErrorMessage(), ProcessTools.CancelLocation.LOCAL);
                break;
            case REMOTE_CANCEL:
            case REMOTE_ERROR:
                asyncShowCancelMessage(process.getPeer(),
                        process.getErrorMessage(), ProcessTools.CancelLocation.REMOTE);
                break;

        }
        return true;
    }


    @Override
    public boolean performCancel()
    {
        ThreadUtils.runSafeAsync("CancelJoinSessionWizard", log,
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        process.localCancel(null, ProcessTools.CancelOption.NOTIFY_PEER);
                    }
                });
        return true;
    }

    /**
     * Get rid of this method, use a listener !
     */
    @Override
    public void cancelWizard(final JID jid, final String errorMsg,
            final ProcessTools.CancelLocation cancelLocation)
    {

        ThreadUtils.runSafeSync(log, new Runnable()
        {
            @Override
            public void run()
            {

                /*
                 * do NOT CLOSE the wizard if it performs async operations
                 *
                 * see performFinish() -> getContainer().run(boolean, boolean,
                 * IRunnableWithProgress)
                 */
                if (accepted)
                {
                    return;
                }

                //todo
                /* Shell shell = JoinSessionWizard.this.getShell();
               if (shell == null || shell.isDisposed())
                   return;

               ((WizardDialog) JoinSessionWizard.this.getContainer()).close();*/

                asyncShowCancelMessage(jid, errorMsg, cancelLocation);
            }
        });
    }

    private void asyncShowCancelMessage(final JID jid, final String errorMsg,
            final ProcessTools.CancelLocation cancelLocation)
    {
        ThreadUtils.runSafeSync(log, new Runnable()
        {
            @Override
            public void run()
            {
                showCancelMessage(jid, errorMsg, cancelLocation);
            }
        });
    }

    private void showCancelMessage(JID jid, String errorMsg,
            ProcessTools.CancelLocation cancelLocation)
    {

        String peer = jid.getBase();

        /* Shell shell = ThreadUtils.getShell();

       if (errorMsg != null) {
           switch (cancelLocation) {
               case LOCAL:
                   DialogUtils.openErrorMessageDialog(shell,
                           Messages.JoinSessionWizard_inv_cancelled,
                           Messages.JoinSessionWizard_inv_cancelled_text
                                   + Messages.JoinSessionWizard_8 + errorMsg);
                   break;
               case REMOTE:
                   DialogUtils.openErrorMessageDialog(shell,

                           Messages.JoinSessionWizard_inv_cancelled, MessageFormat.format(
                           Messages.JoinSessionWizard_inv_cancelled_text2, peer,
                           errorMsg));
           }
       } else {
           switch (cancelLocation) {
               case LOCAL:
                   break;
               case REMOTE:
                   DialogUtils.openInformationMessageDialog(shell,
                           Messages.JoinSessionWizard_inv_cancelled, MessageFormat
                           .format(Messages.JoinSessionWizard_inv_cancelled_text3,
                                   peer));
           }
       } */
    }
}
