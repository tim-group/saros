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

package de.fu_berlin.inf.dpp.intellij.ui.eventhandler;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.IDialogConstants;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.MessageDialogWithToggle;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SarosView;
import de.fu_berlin.inf.dpp.intellij.ui.util.CollaborationUtils;
import org.apache.log4j.Logger;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;


/**
 * Checks if the host remains alone after a user left the session. If so, ask if
 * the session should be closed (optionally remember choice for workspace...)
 *
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class HostLeftAloneInSessionHandler {

    private static Logger LOG = Logger
            .getLogger(HostLeftAloneInSessionHandler.class);

    private final ISarosSessionManager sessionManager;

    private final IPreferenceStore preferenceStore;

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionEnded(ISarosSession session) {
            session.removeListener(projectListener);
            /*
             * we need to clear any open notifications because there might be
             * stuff left, like follow mode notifications, or "buddy joined"
             * notification in case a buddy joined the session but aborted the
             * incoming project negotiation...
             */
            SarosView.clearNotifications();
        }

        @Override
        public void sessionStarted(ISarosSession session) {
            session.addListener(projectListener);
        }
    };

    private final ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void userLeft(User user) {

            ISarosSession session = sessionManager.getSarosSession();

            if (session == null || !session.isHost()
                    || !session.getRemoteUsers().isEmpty())
                return;

            /*
             * only ask to close session if there are no running negotiation
             * processes because if there are, and the last user "left", it was
             * because he cancelled an IncomingProjectNegotiation, and the
             * session will be closed anyway.
             */

            /*
             * Stefan Rossbach: Welcome to global state programming, threading
             * and network latency.
             *
             * This currently does NOT work. It is possible the the project
             * negotiation is about to finish (last cancellation check passed).
             * As we do not have a final synchronization packet in this
             * negotiation process it is possible that the user left packet
             * arrives but cancellation packet arrives lately or never. See
             * stack trace below were the negotiation process passes although
             * Bob leaves the session during negotiation.
             */

            /**
             * Alice side:
             *
             * <pre>
             * DEBUG 16:49:08,878 [main] (HostLeftAloneInSessionHandler.java:44) sessionManager.userLeft
             * INFO  16:49:08,888 [main] (SarosSession.java:612) Buddy [jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros]  left session
             * DEBUG 16:49:08,888 [Thread-217] (BinaryChannelConnection.java:51) SOCKS5 (direct) [jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros] Connection was closed by me. socket closed
             * TRACE 16:49:09,060 [main] (EditorManagerEcl.java:1027) .partActivated invoked
             * TRACE 16:49:09,060 [main] (SharedProjectFileDecorator.java:133) User: jenkins_alice_stf activated an editor -> SPath [editorType=txt, path=src/de/alice/HelloWorld.java, project=java]
             * TRACE 16:49:09,070 [Worker-10] (SharedProjectFileDecorator.java:301) No Deco: L/java/src/de/alice/HelloWorld.java
             * TRACE 16:49:09,083 [Worker-10] (SharedProjectFileDecorator.java:301) No Deco: P/java
             * TRACE 16:49:09,122 [RMI TCP Connection(5)-127.0.0.1] (RemoteWorkbenchBot.java:75) opening view with id: de.fu_berlin.inf.dpp.ui.views.SarosView
             * DEBUG 16:49:09,422 [Worker-9] (OutgoingProjectNegotiation.java:433) OPN [remote side: jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros] : archive send
             * DEBUG 16:49:09,422 [Worker-9] (CancelableProcess.java:280) process OPN [remote side: jenkins_bob_stf@saros-con.imp.fu-berlin.de/Saros] exit status: OK
             * </pre>
             *
             * Bob side:
             *
             * <pre>
             *
             * DEBUG 16:49:09,402 [Worker-6] (FileUtils.java:205) File written to disk: .project
             * DEBUG 16:49:09,402 [Worker-6] (FileUtils.java:209) Unpacked archive in 0 s
             * TRACE 16:49:09,402 [Worker-6] (ChecksumCacheImpl.java:74) invalidating checksum for existing file: /java/.classpath [0x8F53373DCA77CBCA9383FE23E0132271]
             * TRACE 16:49:09,402 [Worker-6] (ChecksumCacheImpl.java:74) invalidating checksum for existing file: /java/.project [0x5A01062F55D09CDA404237A34113627E]
             * TRACE 16:49:09,402 [Worker-6] (ChecksumCacheImpl.java:74) invalidating checksum for existing file: /java/src/de/alice/HelloWorld.java [0xCC20B91A3A971472D6482719A2540DA5]
             * TRACE 16:49:09,453 [main] (EditorPool.java:278) EditorPool.getAllEditors invoked
             * DEBUG 16:49:09,455 [Worker-6] (SharedProjectDecorator.java:80) PROJECT ADDED: 1212967801
             * ERROR 16:49:09,455 [Worker-6] (SarosSessionManager.java:782) Internal error in notifying listener of an added project: java.lang.NullPointerException
             * at de.fu_berlin.inf.dpp.ui.decorators.SharedProjectDecorator$1.projectAdded(SharedProjectDecorator.java:81)
             * at de.fu_berlin.inf.dpp.project.SarosSessionManager.projectAdded(SarosSessionManager.java:780)
             * at de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation.accept(IncomingProjectNegotiation.java:241)
             * at de.fu_berlin.inf.dpp.ui.wizards.AddProjectToSessionWizard$2.run(AddProjectToSessionWizard.java:267)
             * at org.eclipse.core.internal.jobs.Worker.run(Worker.java:54)
             * DEBUG 16:49:09,456 [Worker-6] (CancelableProcess.java:280) process IPN [remote side: jenkins_alice_stf@saros-con.imp.fu-berlin.de/Saros] exit status: OK
             * </pre>
             */

            // if (processes.getProcesses().size() == 0) {
            handleHostLeftAlone();
            // }
        }
    };

    public HostLeftAloneInSessionHandler(
            final ISarosSessionManager sessionManager,
            final IPreferenceStore preferenceStore) {
        this.sessionManager = sessionManager;
        this.preferenceStore = preferenceStore;
        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    private void handleHostLeftAlone() {
        String stopSessionPreference = preferenceStore
                .getString(PreferenceConstants.AUTO_STOP_EMPTY_SESSION);

        boolean prompt = MessageDialogWithToggle.PROMPT
                .equals(stopSessionPreference);

        boolean stopSession = MessageDialogWithToggle.ALWAYS
                .equals(stopSessionPreference);

        if (prompt) {
            MessageDialogWithToggle dialog = MessageDialogWithToggle
                    .openYesNoQuestion(SWTUtils.getShell(),
                            Messages.HostLeftAloneInSessionDialog_title,
                            Messages.HostLeftAloneInSessionDialog_message,
                            "Remember decision", false, preferenceStore,
                            PreferenceConstants.AUTO_STOP_EMPTY_SESSION);

            stopSession = dialog.getReturnCode() == IDialogConstants.YES_ID;
        }

        if (stopSession) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    CollaborationUtils.leaveSession();
                }
            });
        }
    }
}
