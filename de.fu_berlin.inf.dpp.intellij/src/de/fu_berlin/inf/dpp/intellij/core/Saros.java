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

package de.fu_berlin.inf.dpp.intellij.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.context.SarosContext;
import de.fu_berlin.inf.dpp.core.context.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.intellij.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.core.store.PreferenceStore;
import de.fu_berlin.inf.dpp.intellij.context.SarosIntellijContextFactory;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosMainPanelView;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Saros plugin class
 */
//todo: adopted from eclipse
public class Saros
{

    protected static Logger LOG = Logger.getLogger(Saros.class);

    public static Random RANDOM = new Random();

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */

    public static final String SAROS = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$

    /**
     * Default server name
     */
    public static final String SAROS_SERVER = "saros-con.imp.fu-berlin.de";

    /**
     * The name of the XMPP namespace used by SarosEclipse. At the moment it is only
     * used to advertise the SarosEclipse feature in the Service Discovery.
     * <p/>
     * TODO Add version information, so that only compatible versions of SarosEclipse
     * can use each other.
     */
    public final static String NAMESPACE = SAROS;

    /**
     * The name of the resource identifier used by SarosEclipse when connecting to the
     * XMPP server (for instance when logging in as john@doe.com, SarosEclipse will
     * connect using john@doe.com/SarosEclipse)
     * <p/>
     * //todo
     */
    public final static String RESOURCE = "Saros"; //$NON-NLS-1$

    /**
     * Sub-namespace for the server. It is used advertise when a server is
     * active.
     */
    public static final String NAMESPACE_SERVER = NAMESPACE + ".server"; //$NON-NLS-1$

    private static Saros _instance;

    private static boolean isInitialized;

    private static boolean isRunning;

    private Project project;
    private ToolWindow toolWindow;

    private XMPPConnectionService connectionService;
    private ISarosSessionManager sessionManager;
    private DataTransferManager transferManager;

    private XMPPAccountStore accountStore;

    protected PreferenceUtils preferenceUtils;

    private SarosMainPanelView mainPanel;
    private IWorkspace workspace;

    private SarosContext sarosContext;

    /**
     * The secure preferences store, used to store sensitive data that may (at
     * the user's option) be stored encrypted.
     */

    protected IPreferenceStore configPrefs;

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link de.fu_berlin.inf.dpp.intellij.context.SarosContext#reinject(Object)} will be well defined.
     */
    public static boolean isInitialized()
    {
        return isInitialized;
    }

    public static boolean isIsRunning()
    {
        return isRunning;
    }

    public static void checkInitialized()
    {
        if (!isInitialized())
        {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }


    public SarosMainPanelView getMainPanel()
    {
        return mainPanel;
    }

    public void setMainPanel(SarosMainPanelView mainPanel)
    {
        this.mainPanel = mainPanel;
    }

    public static Saros create(Project project, ToolWindow toolWindow)
    {
        if (_instance == null)
        {
            _instance = new Saros(project, toolWindow);
        }
        return _instance;
    }

    /**
     * Instance of Saros
     *
     * @return
     */
    public static Saros instance()
    {
        if (_instance == null)
        {
            throw new Error("Saros not initialized");
        }
        return _instance;
    }


    private Saros(Project project, ToolWindow toolWindow)
    {
        this.project = project;
        this.toolWindow = toolWindow;
        this.isRunning = false;
    }


    public void start()
    {

        if (isInitialized)
        {
            return;
        }

        this.configPrefs = new PreferenceStore();

        //CONTEXT
        this.sarosContext = new SarosContext(new SarosIntellijContextFactory(this,
                new SarosCoreContextFactory()), new DotGraphMonitor());

        SarosPluginContext.setSarosContext(sarosContext);

        connectionService = sarosContext.getComponent(XMPPConnectionService.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        accountStore = sarosContext.getComponent(XMPPAccountStore.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);
        transferManager = sarosContext.getComponent(DataTransferManager.class);

        //todo: set parameters from config
        connectionService.configure(Saros.NAMESPACE, Saros.RESOURCE, false, false, 8888, null, null, true, null, 80, true);

        this.isInitialized = true;
        // Make sure that all components in the container are
        // instantiated
        this.sarosContext.getComponents(Object.class);


    }


    public void stop()
    {

        this.isInitialized = false;
    }


    public boolean isConnected()
    {
        return connectionService.isConnected();
    }

    /**
     * @return
     */
    public XMPPConnectionService getConnectionService()
    {
        return connectionService;
    }

    public ISarosSessionManager getSessionManager()
    {
        return sessionManager;
    }


    public XMPPAccountStore getAccountStore()
    {
        if (accountStore == null)
        {
            this.accountStore = new XMPPAccountStore();
            this.accountStore.setAccountFile(new File("SarosXMPPAccount.info"), "saros123");
            //  MockInitializer.initAccountStore(accountStore); //todo
        }
        return accountStore;
    }


    public IPreferenceStore getConfigPrefs()
    {
        return configPrefs;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }


    public ToolWindow getToolWindow()
    {
        return toolWindow;
    }


    /**
     * Connects using the active account from the {@link de.fu_berlin.inf.dpp.core.account.XMPPAccountStore}. If
     * no active account is present a wizard is opened before.
     * <p/>
     * If there is already an established connection when calling this method,
     * it disconnects before connecting (including state transitions!).
     *
     * @param failSilently if set to <code>true</code> a connection failure will not be
     *                     reported to the user
     * @blocking
     * @see de.fu_berlin.inf.dpp.core.account.XMPPAccountStore#setAccountActive(XMPPAccount)
     */
    public void connect(boolean failSilently)
    {

        /*
         * the Saros Configuration Wizard may call this again when invoking the
         * configureXMPPAccount method call, so abort here to prevent an already
         * logged in error
         */

        // FIXME this "logic" should not be done here !

        if (accountStore.isEmpty())
        {
            boolean configured = /*
                                  * side effect to:
                                  * preferenceUtils.isAutoConnecting()
                                  */
                    configureXMPPAccount();

            if (!configured
                    || (configured && preferenceUtils.isAutoConnecting()))
            {
                return;
            }
        }

        XMPPAccount account = accountStore.getActiveAccount();

        String username = account.getUsername();
        String password = account.getPassword();
        String domain = account.getDomain();
        String server = account.getServer();
        int port = account.getPort();
        boolean useTLS = account.useTLS();
        boolean useSASL = account.useSASL();

        connectionService.disconnect();

        List<String> socks5Candidates = preferenceUtils.getSocks5Candidates();

        connectionService.configure(NAMESPACE, RESOURCE,
                preferenceUtils.isDebugEnabled(),
                preferenceUtils.isLocalSOCKS5ProxyEnabled(),
                preferenceUtils.getFileTransferPort(), socks5Candidates,
                preferenceUtils.getAutoPortmappingGatewayID(),
                preferenceUtils.useExternalGatewayAddress(),
                preferenceUtils.getStunIP(), preferenceUtils.getStunPort(),
                preferenceUtils.isAutoPortmappingEnabled());

        Exception connectionError = null;

        try
        {

            if (preferenceUtils.forceFileTranserByChat())
            {
                transferManager.setTransport(DataTransferManager.IBB_TRANSPORT);
            }
            else
            {
                transferManager.setTransport(/* use all */-1);
            }

            connectionService.connect(
                    createConnectionConfiguration(domain, server, port, useTLS,
                            useSASL), username, password
            );
        }
        catch (Exception e)
        {
            connectionError = e;
        }

        if (connectionError == null)
        {
            return;
        }

        try
        {
            if (!(connectionError instanceof XMPPException))
            {
                throw connectionError;
            }

        }
        catch (Exception e)
        {
            LOG.error("internal error while connecting to the XMPP server: "
                    + e.getMessage(), e);

        }
    }

    protected ConnectionConfiguration createConnectionConfiguration(
            String domain, String server, int port, boolean useTLS, boolean useSASL)
    {


        ConnectionConfiguration connectionConfiguration = null;

        if (server.length() == 0)
        {
            connectionConfiguration = new ConnectionConfiguration(domain);

        }
        else
        {
            connectionConfiguration = new ConnectionConfiguration(server, port,
                    domain);
        }

        connectionConfiguration.setSASLAuthenticationEnabled(useSASL);

        if (!useTLS)
        {
            connectionConfiguration
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        }

        connectionConfiguration.setReconnectionAllowed(false);

        return connectionConfiguration;
    }


    public boolean configureXMPPAccount()
    {

        //todo: make implementation

        return true;
    }

    public SarosContext getSarosContext()
    {
        return sarosContext;
    }


    public IWorkspace getWorkspace()
    {
        return workspace;
    }

    public void setWorkspace(IWorkspace workspace)
    {
        this.workspace = workspace;
    }
}
