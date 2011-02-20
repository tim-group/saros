package de.fu_berlin.inf.dpp.stf.client.wrapper;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;

public class Bot extends Wrapper {

    public Bot(Tester tester) {
        super(tester);
        // TODO Auto-generated constructor stub
    }

    // public STFBotView view(String viewTitle) throws RemoteException {
    // tester.view.setViewTitle(viewTitle);
    // return tester.view;
    // }

    // public STFBotShell shell(String title) throws RemoteException {
    // tester.shell.setShellTitle(title);
    // return tester.shell;
    // }

    public STFBot bot() throws RemoteException {
        return tester.bot;
    }
}