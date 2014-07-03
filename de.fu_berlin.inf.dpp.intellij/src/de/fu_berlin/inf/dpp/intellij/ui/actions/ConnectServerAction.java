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

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.intellij.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;

import javax.swing.*;

/**
 * Connects XMPP/Jabber server with given account
 */
public class ConnectServerAction extends AbstractSarosAction implements IConnectionAction {
    public static final String NAME = "connect";

    private String activeUser;
    private boolean createNew = false;

    @Override
    public String getActionName() {
        return NAME;
    }

    /**
     * Sets active user to connect as
     *
     * @param activeUser
     */
    public void setActiveUser(String activeUser) {
        this.activeUser = activeUser;
    }

    /**
     * @param createNew
     */
    public void setCreateNew(boolean createNew) {
        this.createNew = createNew;
    }

    /**
     * Searches for user in account store
     *
     * @param user
     * @return
     */
    protected XMPPAccount locateAccount(String user) {
        int index = user.indexOf("@");
        String server = null;
        if (index > -1) {
            String[] pair = user.split("@");
            user = pair[0];
            server = pair[1];
        }

        for (XMPPAccount account : saros.getAccountStore().getAllAccounts()) {
            if (server == null) {
                if (user.equalsIgnoreCase(account.getUsername())) {
                    return account;
                }
            } else {
                if (server.equalsIgnoreCase(account.getServer()) && user.equalsIgnoreCase(account.getUsername())) {
                    return account;
                }
            }


            if (user.startsWith(account.getUsername())) {
                return account;
            }
        }

        return null;
    }

    @Override
    public void run() {
        XMPPAccount account;
        boolean isNew = false;
        if (activeUser != null) {
            account = locateAccount(activeUser);
            activeUser = null; //removeAll user name
        } else if (createNew || saros.getAccountStore().isEmpty()) {
            //throw new RuntimeException("No current account set!"); //todo: open dialog


            final String jabberID = SafeDialogUtils.showInputDialog("Your Jabber-ID (e.g. 'dev1_alice_stf')", "dev1_alice_stf", "Login");
            if (jabberID == null) {
                actionFinished();
                return;
            }
            final String password = SafeDialogUtils.showInputDialog("Password (e.g. 'dev')", "dev", "Login");
            if (password == null) {
                actionFinished();
                return;
            }
            account = saros.getAccountStore().createAccount(jabberID, password, Saros.NAMESPACE, Saros.SAROS_SERVER, 80, false, false);
            //account = new XMPPAccount(jabberID, password, Saros.NAMESPACE, Saros.SAROS_SERVER, 80, false, false);
            isNew = true;
        } else {
            account = saros.getAccountStore().getActiveAccount();
        }

        LOG.info("Connecting server: [" + account.getUsername() + "@" + account.getServer() + "]");

        try {


            saros.getConnectionService().connect(new ConnectionConfiguration(account.getServer()), account.getUsername(), account.getPassword());

            //store account
            if (isNew &&
                    !saros.getAccountStore().exists(account.getUsername(), account.getDomain(), account.getServer(), account.getPort())) {

                account = saros.getAccountStore().createAccount(account.getUsername(), account.getPassword(), account.getDomain(), account.getServer(), account.getPort(), account.useTLS(), account.useSASL());
            }
            saros.getAccountStore().setAccountActive(account);
        } catch (XMPPException e) {
            // Messages.showErrorDialog("Bad login or password. Try again!","Error");
            JOptionPane.showMessageDialog(guiFrame, "Bad login or password. Try again!", "Error", JOptionPane.ERROR_MESSAGE);

            LOG.error(e);
        }


        actionFinished();
    }
}
