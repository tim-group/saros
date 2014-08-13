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
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;

import javax.swing.JOptionPane;

/**
 * Connects XMPP/Jabber server with given account
 */
public class ConnectServerAction extends AbstractSarosAction {
    public static final String NAME = "connect";

    private String activeUser;
    private boolean createNew = false;

    @Inject
    private XMPPAccountStore accountStore;

    @Inject
    private XMPPConnectionService connectionService;

    public ConnectServerAction() {
        SarosPluginContext.initComponent(this);
    }

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

    @Override
    public void run() {
/*
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override public void run() {
                try {
                    ModuleManager moduleManager = ModuleManager.getInstance(saros.getProject());
                    moduleManager.newModule(
                        saros.getProject().getBasePath() + "/" + "testcreate/testcreate.iml",
                        StdModuleTypes.JAVA.getId());

                    ModuleManager moduleManager = ModuleManager.getInstance(saros.getProject());
                    final ModifiableModuleModel moduleModel = moduleManager
                        .getModifiableModel();
                    String moduleFilePath = saros.getProject().getBasePath() + "/testnewmodule"
                        + ModuleFileType.DOT_DEFAULT_EXTENSION;
                    new JavaModuleType().createModuleBuilder().createModule(
                        moduleModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/



        XMPPAccount account;
        boolean isNew = false;
        if (activeUser != null) {
            account = locateAccount(activeUser);
            activeUser = null; //removeAll user name
        } else if (createNew || accountStore.isEmpty()) {
            final String jabberID = SafeDialogUtils
                .showInputDialog("Your Jabber-ID (e.g. 'dev1_alice_stf')",
                    "dev1_alice_stf", "Login");
            if (jabberID == null) {
                actionFinished();
                return;
            }
            final String password = SafeDialogUtils
                .showInputDialog("Password (e.g. 'dev')", "dev", "Login");
            if (password == null) {
                actionFinished();
                return;
            }
            account = accountStore
                .createAccount(jabberID, password, Saros.NAMESPACE,
                    Saros.SAROS_SERVER, 80, false, false);
            isNew = true;
        } else {
            account = accountStore.getActiveAccount();
        }

        LOG.info("Connecting server: [" + account.getUsername() + "@" + account
            .getServer() + "]");

        try {
            connectionService
                .connect(new ConnectionConfiguration(account.getServer()),
                    account.getUsername(), account.getPassword());

            //store account
            if (isNew && !accountStore
                .exists(account.getUsername(), account.getDomain(),
                    account.getServer(), account.getPort())) {

                account = accountStore
                    .createAccount(account.getUsername(), account.getPassword(),
                        account.getDomain(), account.getServer(),
                        account.getPort(), account.useTLS(), account.useSASL());
            }
            accountStore.setAccountActive(account);
        } catch (XMPPException e) {
            JOptionPane.showMessageDialog(guiFrame,
                "Bad login or password. Try again!", "Error",
                JOptionPane.ERROR_MESSAGE);
            LOG.error(e);
        }

        actionFinished();
    }
}
