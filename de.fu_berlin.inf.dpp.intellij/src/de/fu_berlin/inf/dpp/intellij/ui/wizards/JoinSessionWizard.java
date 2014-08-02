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

import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.PageActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.Wizard;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPage;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.awt.Container;
import java.text.MessageFormat;

/**
 * A wizard that guides the user through an incoming invitation process.
 * <p/>
 * more nicely: Long-Running Operation after each step, cancellation by a remote
 * party, auto-advance.
 */
public class JoinSessionWizard {
    public static final String PAGE_INFO_ID = "JoinSessionInfo";
    public static final String PAGE_PROGRESS_ID = "JoinSessionProgress";

    private Container parent;

    @Inject
    private static Saros saros;

    private static final Logger LOG = Logger.getLogger(JoinSessionWizard.class);

    private boolean accepted = false;

    private IncomingSessionNegotiation process;

    private SessionNegotiation.Status invitationStatus;

    private ProgressPage progressPage;
    private Wizard wizard;

    private PageActionListener actionListener = new PageActionListener() {
        @Override
        public void back() {

        }

        @Override
        public void next() {
            performFinish();
        }

        @Override
        public void cancel() {
            performCancel();
        }
    };

    /**
     * Creates wizard UI
     *
     * @param process
     */
    public JoinSessionWizard(IncomingSessionNegotiation process) {
        this.process = process;
        SarosPluginContext.initComponent(this);
        parent = saros.getMainPanel();

        wizard = new Wizard(Messages.JoinSessionWizard_title);
        wizard.getNavigationPanel().setBackButton(null);

        wizard.setHeadPanel(new HeaderPanel(Messages.ShowDescriptionPage_title2,
            Messages.ShowDescriptionPage_description));

        InfoPage infoPage = new InfoPage(PAGE_INFO_ID);
        infoPage.addText(process.getPeer().getName() + " "
            + Messages.JoinSessionWizard_info);
        infoPage.addText(process.getDescription());
        infoPage.addPageListener(actionListener);
        infoPage.setNextButtonTitle(Messages.JoinSessionWizard_accept);

        wizard.registerPage(infoPage);

        this.progressPage = new ProgressPage(PAGE_PROGRESS_ID);
        wizard.registerPage(progressPage);

        wizard.create();

    }

    public boolean performFinish() {

        accepted = true;

        try {

            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    IProgressMonitor progress = progressPage
                        .getProgressMonitor(true, true);
                    invitationStatus = process.accept(progress);
                    switch (invitationStatus) {
                    case OK:
                        break;
                    case CANCEL:
                    case ERROR:
                        asyncShowCancelMessage(process.getPeer(),
                            process.getErrorMessage(), CancelLocation.LOCAL);
                        break;
                    case REMOTE_CANCEL:
                    case REMOTE_ERROR:
                        asyncShowCancelMessage(process.getPeer(),
                            process.getErrorMessage(), CancelLocation.REMOTE);
                        break;

                    }
                }
            });

        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause == null) {
                cause = e;
            }

            asyncShowCancelMessage(process.getPeer(), cause.getMessage(),
                CancelLocation.LOCAL);

            // give up, close the wizard as we cannot do anything here !
            return accepted;
        }

        return accepted;
    }

    public boolean performCancel() {
        ThreadUtils
            .runSafeAsync("CancelJoinSessionWizard", LOG, new Runnable() {
                    @Override
                    public void run() {
                        process.localCancel(null, CancelOption.NOTIFY_PEER);
                    }
                }
            );
        return true;
    }

    /**
     * Get rid of this method, use a listener !
     */
    public void cancelWizard(final JID jid, final String errorMsg,
        final CancelLocation cancelLocation) {

        ThreadUtils.runSafeSync(LOG, new Runnable() {
            @Override
            public void run() {

                /*
                 * do NOT CLOSE the wizard if it performs async operations
                 *
                 * see performFinish() -> getContainer().run(boolean, boolean,
                 * IRunnableWithProgress)
                 */
                if (accepted) {
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
        final CancelLocation cancelLocation) {
        ThreadUtils.runSafeAsync(LOG, new Runnable() {
            @Override
            public void run() {
                showCancelMessage(jid, errorMsg, cancelLocation);
            }
        });
    }

    private void showCancelMessage(JID jid, String errorMsg,
        CancelLocation cancelLocation) {

        String peer = jid.getBase();

        Container shell = parent;

        if (errorMsg != null) {
            switch (cancelLocation) {
            case LOCAL:
                DialogUtils
                    .showError(shell, Messages.JoinSessionWizard_inv_cancelled,
                        Messages.JoinSessionWizard_inv_cancelled_text
                            + Messages.JoinSessionWizard_8 + errorMsg
                    );
                break;
            case REMOTE:
                DialogUtils.showError(shell,

                    Messages.JoinSessionWizard_inv_cancelled, MessageFormat
                        .format(Messages.JoinSessionWizard_inv_cancelled_text2,
                            peer, errorMsg)
                );
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                DialogUtils
                    .showInfo(shell, Messages.JoinSessionWizard_inv_cancelled,
                        MessageFormat.format(
                            Messages.JoinSessionWizard_inv_cancelled_text3,
                            peer)
                    );
            }
        }
    }
}
