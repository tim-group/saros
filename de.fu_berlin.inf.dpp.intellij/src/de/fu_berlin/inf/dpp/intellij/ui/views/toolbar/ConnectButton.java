package de.fu_berlin.inf.dpp.intellij.ui.views.toolbar;

import de.fu_berlin.inf.dpp.core.account.XMPPAccount;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.ConnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.DisconnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.ISarosActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.SarosActionFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Implementation of connect XMPP/jabber server button
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.21
 * Time: 07.39
 */
public class ConnectButton extends ToolbarButton implements ISarosActionListener
{
    private JPopupMenu popupMenu = new JPopupMenu();

    private ISarosAction disconnectAction = SarosActionFactory.getAction(DisconnectServerAction.NAME);
    private ConnectServerAction connectAction = SarosActionFactory.getConnectServerAction();

    public ConnectButton()
    {
        createButton();
    }

    private void createButton()
    {
        setIcon("connect", "Connect");
        setActionCommand(ConnectServerAction.NAME);

        setToolTipText("Connect to XMPP/jabber server");

        createMenu();
        connectAction.addActionListener(this);   //register listener

        disconnectAction.setGuiFrame(this);
        connectAction.setGuiFrame(this);

        final JButton button = this;
        final Saros saros = this.saros;
        this.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ev)
            {

                if (saros.getAccountStore().isEmpty())
                {
                    button.setEnabled(false);
                    startAction();
                }
                else
                {
                    popupMenu.show(button, 0, button.getBounds().y + button.getBounds().height);
                }
            }
        });
    }

    private JPopupMenu createMenu()
    {

        final JButton button = this;
        //set accounts
        final List<XMPPAccount> accounts = saros.getAccountStore().getAllAccounts();
        for (XMPPAccount account : accounts)
        {
            // final String userName = account.getUsername();
            // JMenuItem accountItem = new JMenuItem(account.getUsername() + "@" + account.getServer());
            final String userName = account.getUsername() + "@" + account.getServer();
            JMenuItem accountItem = new JMenuItem(userName);
            accountItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    button.setEnabled(false);
                    connectAction.setActiveUser(userName);
                    startAction(connectAction);
                }
            });
            popupMenu.add(accountItem);
        }

        popupMenu.addSeparator();

        JMenuItem menuItemAdd = new JMenuItem("Add account...");
        menuItemAdd.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                button.setEnabled(false);
                createNewAccount();
            }
        });
        popupMenu.add(menuItemAdd);

        JMenuItem configure = new JMenuItem("Configure accounts...");
        configure.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                configureAccounts();
            }
        });
        popupMenu.add(configure);

        JMenuItem disconnect = new JMenuItem("Disconnect server");
        disconnect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
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
    protected void createNewAccount()
    {
        connectAction.setCreateNew(true);
        startAction(connectAction);
    }

    /**
     *
     */
    protected void configureAccounts()
    {
        log.debug("ConnectButton.actionPerformed CONFIGURE");

        throw new IllegalStateException("Not implemented!");
    }

    @Override
    public void actionStarted(ISarosAction action)
    {

    }

    @Override
    public void actionFinished(ISarosAction action)
    {
        popupMenu.removeAll();
        createMenu();
    }

    public ISarosAction getDisconnectAction()
    {
        return disconnectAction;
    }

    public ConnectServerAction getConnectAction()
    {
        return connectAction;
    }
}
