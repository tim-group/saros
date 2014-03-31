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

package de.fu_berlin.inf.dpp.core.ui;

import java.awt.*;
import java.util.concurrent.Callable;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.codehaus.groovy.tools.shell.Shell;


public class DialogUtils {

    private static Logger log = Logger.getLogger(DialogUtils.class);

    private DialogUtils() {
        // no instantiation allowed
    }

    /**
     * Calls open() on the given window and returns the result.
     */
    public static int openWindow(IWizardDialogAccessable wd) {

        System.out.println("DialogUtils.openWindow");
//        if (wd.getShell() == null || wd.getShell().isDisposed()) {
//            wd.create();
//        }
//        wd.getShell().open();
//        return wd.open();

        //todo
        return -1;

    }

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#ERROR} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * taskbar that the application wants focus).
     *
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return
     */
    public static int openErrorMessageDialog(Shell shell, String dialogTitle,
            String dialogMessage) {

        System.out.println("DialogUtils.openErrorMessageDialog");
//        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
//                dialogMessage, MessageDialog.ERROR,
//                new String[] { IDialogConstants.OK_LABEL }, 0);
//        return openWindow(md);
        //todo
        return -1;
    }

    /**
     * Shows an error window and sets monitors subTask to <code>message</code>
     * or exceptions message.
     *
     * @param title
     *            Title of error window
     * @param message
     *            Message of error window
     * @param e
     *            Exception caused this error, may be <code>null</code>
     * @param monitor
     *            May be <code>null</code>
     */
    public static void showErrorPopup(final Logger log, final String title,
            final String message, Exception e, IProgressMonitor monitor) {
       ThreadUtils.runSafeSync(log, new Runnable()
       {
           @Override
           public void run()
           {
               System.out.println("DialogUtils.run");
               //todo
//               DialogUtils.openErrorMessageDialog(runSafeSync.getShell(), title,
//                       message);
           }
       });
        if (monitor != null) {
            if (e != null && e.getMessage() != null
                    && !(e.getMessage().length() == 0))
                monitor.subTask(e.getMessage());
            else
                monitor.subTask(message);
        }
    }

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#INFORMATION} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * taskbar that the application wants focus).
     *
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return
     */
    public static int openInformationMessageDialog(Shell shell,
            String dialogTitle, String dialogMessage) {

        System.out.println("DialogUtils.openInformationMessageDialog");
        //todo
//        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
//                dialogMessage, MessageDialog.INFORMATION,
//                new String[] { IDialogConstants.OK_LABEL }, 0);
//        return openWindow(md);

        return -1;
    }

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#WARNING} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * taskbar that the application wants focus).
     *
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return
     */
    public static int openWarningMessageDialog(Shell shell, String dialogTitle,
            String dialogMessage) {
//        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
//                dialogMessage, MessageDialog.WARNING,
//                new String[] { IDialogConstants.OK_LABEL }, 0);
//        return openWindow(md);

        System.out.println("DialogUtils.openWarningMessageDialog");

        //todo
        return -1;
    }

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#QUESTION} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * task-bar that the application wants focus).
     *
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return true if the user answered with YES
     */
    public static boolean openQuestionMessageDialog(Shell shell,
            String dialogTitle, String dialogMessage) {
//        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
//                dialogMessage, MessageDialog.QUESTION, new String[] {
//                IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
//        return openWindow(md) == 0;
        System.out.println("DialogUtils.openQuestionMessageDialog");
        //todo
        return true;
    }

    /**
     * Ask the User a given question. It pops up a QuestionDialog with given
     * title and message. Additionally custom button labels are applicable.
     *
     * @param title
     *            dialog title
     * @param message
     *            displayed message
     * @param dialogButtonLabels
     *            custom button labels
     * @param failSilently
     *            don`t open the dialog
     *
     * @return boolean indicating whether the user said Yes or No
     */
    public static boolean popUpCustomQuestion(final String title,
            final String message, final String[] dialogButtonLabels,
            boolean failSilently) {
        if (failSilently)
            return false;

        System.out.println("DialogUtils.popUpCustomQuestion");
        try {
           /* return ThreadUtils.runSafeSync(new Callable<Boolean>() {
                @Override
                public Boolean call() {
//                    MessageDialog md = new MessageDialog(SWTUtils.getShell(),
//                            title, null, message, MessageDialog.QUESTION,
//                            dialogButtonLabels, 0);
//                    md.open();
//                    return md.getReturnCode() == 0;

                    //todo
                    return true;
                }
            });*/

            //todo
            return true;
        } catch (Exception e) {
            log.error("An internal error ocurred while trying"
                    + " to open the question dialog.");
            return false;
        }
    }

    /**
     * Ask the User a given question. It pops up a QuestionDialog with given
     * title and message.
     *
     * @return boolean indicating whether the user said Yes or No
     */
    public static boolean popUpYesNoQuestion(final String title,
            final String message, boolean failSilently) {
        if (failSilently)
            return false;

        System.out.println("DialogUtils.popUpYesNoQuestion");
/*
        try {
            return ThreadUtils.runSafeSync(new Callable<Boolean>() {
                @Override
                public Boolean call() {
//                    return MessageDialog.openQuestion(SWTUtils.getShell(),
//                            title, message);

                    return true;
                }
            });
        } catch (Exception e) {
            log.error("An internal error ocurred while trying"
                    + " to open the question dialog.");
            return false;
        }*/
        //todo
        return true;
    }

    /**
     * Indicate the User that there was an error. It pops up an ErrorDialog with
     * given title and message.
     */
    public static void popUpFailureMessage(final String title,
            final String message, boolean failSilently) {
        if (failSilently)
            return;

        ThreadUtils.runSafeSync(log, new Runnable()
        {
            @Override
            public void run()
            {

                System.out.println("DialogUtils.run");
                // MessageDialog.openError(SWTUtils.getShell(), title, message);
            }
        });
    }
}
