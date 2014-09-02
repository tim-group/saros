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

package de.fu_berlin.inf.dpp.intellij.ui.views.toolbar;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.actions.*;
import org.picocontainer.annotations.Inject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Implementation of connect XMPP/jabber server button
 */
public class ConnectButton extends ToolbarButton implements SarosActionListener {
    public static final String CONNECT_ICON_PATH = "icons/elcl16/connect.png";
    private JPopupMenu popupMenu = new JPopupMenu();

    private final AbstractSarosAction disconnectAction;
    private final ConnectServerAction connectAction;

    @Inject
    private XMPPAccountStore accountStore;

    public ConnectButton() {
        SarosPluginContext.initComponent(this);
        disconnectAction = SarosActionFactory.getAction(DisconnectServerAction.NAME);
        connectAction = (ConnectServerAction)SarosActionFactory.getAction(ConnectServerAction.NAME);
        createButton();
    }

    private void createButton() {
        setIcon(CONNECT_ICON_PATH, "Connect");
        setActionCommand(ConnectServerAction.NAME);

        setToolTipText("Connect to XMPP/jabber server");

        createMenu();
        connectAction.addActionListener(this);   //register listener

        disconnectAction.setGuiFrame(this);
        connectAction.setGuiFrame(this);

        final JButton button = this;
        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {

                if (accountStore.isEmpty()) {
                    button.setEnabled(false);
                    startAction(connectAction);
                } else {
                    popupMenu.show(button, 0, button.getBounds().y + button.getBounds().height);
                }
            }
        });
    }

    private JPopupMenu createMenu() {

        final JButton button = this;
        //set accounts
        final List<XMPPAccount> accounts = accountStore.getAllAccounts();
        for (XMPPAccount account : accounts) {
            // final String userName = account.getUsername();
            // JMenuItem accountItem = new JMenuItem(account.getUsername() + "@" + account.getServer());
            final String userName = account.getUsername() + "@" + account.getServer();
            JMenuItem accountItem = new JMenuItem(userName);
            accountItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    button.setEnabled(false);
                    connectAction.setActiveUser(userName);
                    startAction(connectAction);
                }
            });
            popupMenu.add(accountItem);
        }

        popupMenu.addSeparator();

        JMenuItem menuItemAdd = new JMenuItem("Add account...");
        menuItemAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button.setEnabled(false);
                createNewAccount();
            }
        });
        popupMenu.add(menuItemAdd);

        JMenuItem configure = new JMenuItem("Configure accounts...");
        configure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configureAccounts();
            }
        });
        popupMenu.add(configure);

        JMenuItem disconnect = new JMenuItem("Disconnect server");
        disconnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button.setEnabled(false);
                startAction(disconnectAction);
            }
        });
        popupMenu.add(disconnect);

        return popupMenu;

    }

    /**
     *
     */
    protected void createNewAccount() {
        connectAction.setCreateNew(true);
        startAction(connectAction);
    }

    /**
     *
     */
    protected void configureAccounts() {
        LOG.debug("ConnectButton.actionPerformed CONFIGURE");

        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void actionStarted(AbstractSarosAction action) {

    }

    @Override
    public void actionFinished(AbstractSarosAction action) {
        popupMenu.removeAll();
        createMenu();
    }

    public AbstractSarosAction getDisconnectAction() {
        return disconnectAction;
    }

    public ConnectServerAction getConnectAction() {
        return connectAction;
    }
}
