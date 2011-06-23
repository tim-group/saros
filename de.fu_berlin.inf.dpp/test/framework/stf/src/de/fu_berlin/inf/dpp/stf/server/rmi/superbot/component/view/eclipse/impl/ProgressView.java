package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IProgressView;

public class ProgressView extends StfRemoteObject implements IProgressView {

    private static final ProgressView INSTANCE = new ProgressView();

    private IRemoteBotView view;

    public static ProgressView getInstance() {
        return INSTANCE;
    }

    public IProgressView setView(IRemoteBotView view) {
        this.view = view;

        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void removeProgress() throws RemoteException {
        view.bot().toolbarButton().click();

    }

    public void removeProcess(int index) throws RemoteException {
        view.toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
        view.bot().toolbarButton(index).click();

    }

    public boolean existsPorgress() throws RemoteException {
        view.toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
        return view.bot().existsToolbarButton();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilNotExistsProgress() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !existsPorgress();
            }

            public String getFailureMessage() {
                return "there still exist some progresses";
            }
        });

    }

}
