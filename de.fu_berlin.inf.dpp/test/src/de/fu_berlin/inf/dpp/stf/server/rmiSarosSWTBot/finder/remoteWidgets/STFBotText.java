package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

public interface STFBotText extends Remote {

    /**
     * set the given text into the {@link SWTBotText} with the specified
     * 
     * @param text
     *            the text which you want to insert into the text field
     * @param label
     *            the label on the widget.
     * @throws RemoteException
     */
    public void setText(String text) throws RemoteException;

    /**
     * 
     * @param label
     *            the label on the widget.
     * @return the text in the given {@link SWTBotText}
     * @throws RemoteException
     */
    public String getText() throws RemoteException;
}