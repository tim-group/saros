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
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;

import javax.swing.*;

/**
 * Connects to XMPP/Jabber server with given account or active account
 */
public class ConnectServerAction extends AbstractSarosAction {
    public static final String NAME = "connect";

    @Inject
    private XMPPAccountStore accountStore;

    @Inject
    private XMPPConnectionService connectionService;

    @Override
    public String getActionName() {
        return NAME;
    }

    /**
     * Connects with the given user.
     */
    public void executeWithUser(String user) {
        XMPPAccount account = locateAccount(user);
        connectAccount(account);
        actionFinished();
    }

    /**
     * Connects with active account from the {@link XMPPAccountStore}.
     */
    @Override
    public void execute() {
        XMPPAccount account = accountStore.getActiveAccount();
        connectAccount(account);
        actionFinished();
    }

    /**
     * Searches for user in account store
     */
    protected XMPPAccount locateAccount(String user) {
        int index = user.indexOf("@");
        String server = null;
        if (index > -1) {
            String[] pair = user.split("@");
            user = pair[0];
            server = pair[1];
        }

        for (XMPPAccount account : accountStore.getAllAccounts()) {
            if (server == null) {
                if (user.equalsIgnoreCase(account.getUsername())) {
                    return account;
                }
            } else {
                if (server.equalsIgnoreCase(account.getServer()) && user
                    .equalsIgnoreCase(account.getUsername())) {
                    return account;
                }
            }

            if (user.startsWith(account.getUsername())) {
                return account;
            }
        }

        return null;
    }

    private void connectAccount(XMPPAccount account) {
        LOG.info("Connecting server: [" + account.getUsername() + "@" + account
            .getServer() + "]");

        try {
            connectionService
                .connect(new ConnectionConfiguration(account.getServer()),
                    account.getUsername(), account.getPassword());

            if (!accountStore
                .exists(account.getUsername(), account.getDomain(),
                    account.getServer(), account.getPort())) {
                LOG.info("!!!!! IS THIS NECESSARY????");
                account = accountStore
                    .createAccount(account.getUsername(), account.getPassword(),
                        account.getDomain(), account.getServer(),
                        account.getPort(), account.useTLS(), account.useSASL());
            }
            accountStore.setAccountActive(account);
        } catch (XMPPException e) {
            JOptionPane
                .showMessageDialog(null, "Bad login or password. Try again!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            LOG.error(e);
        }

        actionFinished();
    }
}
