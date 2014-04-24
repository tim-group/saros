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
import de.fu_berlin.inf.dpp.core.context.AbstractSaros;
import de.fu_berlin.inf.dpp.core.account.XMPPAccount;
import de.fu_berlin.inf.dpp.core.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.context.SarosContext;
import de.fu_berlin.inf.dpp.core.context.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.feedback.FeedbackPreferences;
import de.fu_berlin.inf.dpp.core.net.BlockableTransport;
import de.fu_berlin.inf.dpp.core.net.Transport;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.ISecurePreferences;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.core.store.PreferenceStore;
import de.fu_berlin.inf.dpp.intellij.core.store.SecurePreferenceStore;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.intellij.project.intl.WorkspaceIntl;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosMainPanelView;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.proxy.ProxyInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Saros plugin class
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class Saros extends AbstractSaros
{

    private Project project;

    private static Saros _instance;
    private boolean bTesting;


    private XMPPConnectionService connectionService;
    private ISarosSessionManager sessionManager;
    private DataTransferManager transferManager;

    private XMPPAccountStore accountStore;


    protected PreferenceUtils preferenceUtils;

    private SarosMainPanelView mainPanel;

    public SarosMainPanelView getMainPanel()
    {
        return mainPanel;
    }

    public void setMainPanel(SarosMainPanelView mainPanel)
    {
        this.mainPanel = mainPanel;
        this.project = mainPanel.getProject();
    }

    /**
     * The secure preferences store, used to store sensitive data that may (at
     * the user's option) be stored encrypted.
     */
    protected ISecurePreferences securePrefs;
    protected IPreferenceStore configPrefs;


    /**
     * @return
     */
    public static Saros instance()
    {
        if (_instance == null)
        {
            try
            {
                _instance = new Saros();
            }
            catch (Exception e)
            {
                log.error("FATAL ERROR: Saros could not initialize properly!");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return _instance;
    }

    protected SarosContext sarosContext;

    /**
     *
     */
    private Saros() throws Exception
    {

    }

    public void start(boolean testing)
    {
        this.bTesting = testing;
        start();
    }

    public void start()
    {
        System.out.println("Saros.start");

        if (isInitialized)
        {
            return;
        }

        this.securePrefs = new SecurePreferenceStore();
        // MockInitializer.initSecurePrefStore(securePrefs); //todo

        this.configPrefs = new PreferenceStore();
        //  MockInitializer.initPrefStore(configPrefs); //todo


        //CONTEXT
        this.sarosContext = new SarosContext(new SarosIntellijContextFactory(this,
                new SarosCoreContextFactory()), new DotGraphMonitor());

        SarosPluginContext.setSarosContext(sarosContext); //todo: is it needed?

        connectionService = sarosContext.getComponent(XMPPConnectionService.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        accountStore = sarosContext.getComponent(XMPPAccountStore.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);
        transferManager = sarosContext.getComponent(DataTransferManager.class);


        // List<String> socsCandidates =new ArrayList<String>();
        //  socsCandidates.add("192.168.10.145");
        // connectionService.configure(Saros.NAMESPACE,Saros.RESOURCE,true,true,8888,socsCandidates,null,true,null,80,true);   //todo: set parameters from config
        connectionService.configure(Saros.NAMESPACE, Saros.RESOURCE, true, false, 8888, null, null, true, null, 80, true);

        // Make sure that all components in the container are
        // instantiated
        this.sarosContext.getComponents(Object.class);

        FeedbackPreferences.setPreferences(new SarosPreferences());

        this.isInitialized = true;

    }


    public void stop()
    {
        System.out.println("Saros.stop");

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

    public DataTransferManager getTransferManager()
    {
        if (transferManager == null)
        {
            //todo
            final CountDownLatch connectAcknowledge = new CountDownLatch(1);
            final CountDownLatch connectProceed = new CountDownLatch(1);
            Transport fallbackTransport = new Transport(NetTransferMode.IBB);
            BlockableTransport mainTransport = new BlockableTransport(
                    new HashSet<JID>(), NetTransferMode.SOCKS5_DIRECT,
                    connectAcknowledge, connectProceed);

            this.transferManager = new DataTransferManager(connectionService, null, mainTransport, fallbackTransport); //todo
        }

        return transferManager;
    }

    public XMPPAccountStore getAccountStore()
    {
        if (accountStore == null)
        {
            this.accountStore = new XMPPAccountStore(configPrefs, securePrefs);
            //  MockInitializer.initAccountStore(accountStore); //todo
        }
        return accountStore;
    }


    public IPreferenceStore getConfigPrefs()
    {
        return configPrefs;
    }

    public ISecurePreferences getSecurePrefs()
    {
        return securePrefs;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    /* public void start(BundleContext context) throws Exception {

    super.start(context);

    setupLoggers();

    sarosVersion = getBundle().getVersion().toString();

    log.info("Starting Saros " + sarosVersion + " running:\n"
            + Utils.getPlatformInfo());

    sarosContext = new SarosContext(new SarosEclipseContextFactory(this,
            new SarosCoreContextFactory()), dotMonitor);

    SarosPluginContext.setSarosContext(sarosContext);

    sarosFeatureID = SAROS + "_" + sarosVersion; //$NON-NLS-1$

    // Remove the Bundle if an instance of it was already registered
    sarosContext.removeComponent(Bundle.class);
    sarosContext.addComponent(Bundle.class, getBundle());

    *//*
         * must invoked here otherwise some components will fail to initialize
         * due NPE... see getSarosNet()
         *//*
        connectionService = sarosContext
                .getComponent(XMPPConnectionService.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        xmppAccountStore = sarosContext.getComponent(XMPPAccountStore.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);
        transferManager = sarosContext.getComponent(DataTransferManager.class);

        // Make sure that all components in the container are
        // instantiated
        sarosContext.getComponents(Object.class);

        isInitialized = true;

        *//*
         * If other colors than the ones we support are set in the
         * PreferenceStore, overwrite them
         *//*
        SarosAnnotation.resetColors();

        *//*
         * Hack for MARCH 2013 release, ensure a good favorite color
         * distribution for upgrading clients
         *//*

        int favoriteColorID = preferenceUtils.getFavoriteColorID();

        if (!UserColorID.isValid(favoriteColorID)
                && getPreferenceStore().getBoolean(
                "FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR")) {
            favoriteColorID = new Random().nextInt(SarosAnnotation.SIZE);
            log.debug("autogenerated favorite color id is: " + favoriteColorID);
            getPreferenceStore().setValue(
                    PreferenceConstants.FAVORITE_SESSION_COLOR_ID, favoriteColorID);
        }

        getPreferenceStore().setValue(
                "FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR", false);
    }
*/
    /**
     * This method is called upon plug-in activation
     *//*
    @Override
    public void start(BundleContext context) throws Exception {

        super.start(context);

        setupLoggers();

        sarosVersion = getBundle().getVersion().toString();

        log.info("Starting Saros " + sarosVersion + " running:\n"
                + Utils.getPlatformInfo());

        sarosContext = new SarosContext(new SarosEclipseContextFactory(this,
                new SarosCoreContextFactory()), dotMonitor);

        SarosPluginContext.setSarosContext(sarosContext);

        sarosFeatureID = SAROS + "_" + sarosVersion; //$NON-NLS-1$

        // Remove the Bundle if an instance of it was already registered
        sarosContext.removeComponent(Bundle.class);
        sarosContext.addComponent(Bundle.class, getBundle());

        *//*
         * must invoked here otherwise some components will fail to initialize
         * due NPE... see getSarosNet()
         *//*
        connectionService = sarosContext
                .getComponent(XMPPConnectionService.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        xmppAccountStore = sarosContext.getComponent(XMPPAccountStore.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);
        transferManager = sarosContext.getComponent(DataTransferManager.class);

        // Make sure that all components in the container are
        // instantiated
        sarosContext.getComponents(Object.class);

        isInitialized = true;

        *//*
         * If other colors than the ones we support are set in the
         * PreferenceStore, overwrite them
         *//*
        SarosAnnotation.resetColors();

        *//*
         * Hack for MARCH 2013 release, ensure a good favorite color
         * distribution for upgrading clients
         *//*

        int favoriteColorID = preferenceUtils.getFavoriteColorID();

        if (!UserColorID.isValid(favoriteColorID)
                && getPreferenceStore().getBoolean(
                "FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR")) {
            favoriteColorID = new Random().nextInt(SarosAnnotation.SIZE);
            log.debug("autogenerated favorite color id is: " + favoriteColorID);
            getPreferenceStore().setValue(
                    PreferenceConstants.FAVORITE_SESSION_COLOR_ID, favoriteColorID);
        }

        getPreferenceStore().setValue(
                "FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR", false);
    }*/


    //****************************

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

        //  List<String> socks5Candidates = null;
        List<String> socks5Candidates = preferenceUtils.getSocks5Candidates();

        //      if (socks5Candidates.isEmpty())
        //         socks5Candidates = null;

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
            {

//            if (DialogUtils
//                    .popUpYesNoQuestion(
//                            Messages.Saros_connecting_error_title,
//                            generateHumanReadableErrorMessage((XMPPException) connectionError),
//                            failSilently)) {
//
//                if (configureXMPPAccount())
//                    connect(failSilently);
            }
        }
        catch (Exception e)
        {
            log.error("internal error while connecting to the XMPP server: "
                    + e.getMessage(), e);


            System.out.println("Saros.connect");
        }
    }

    protected ConnectionConfiguration createConnectionConfiguration(
            String domain, String server, int port, boolean useTLS, boolean useSASL)
    {

        ProxyInfo proxyInfo;

        if (server.length() != 0)
        {
            proxyInfo = getProxyInfo(server);
        }
        else
        {
            proxyInfo = getProxyInfo(domain);
        }

        ConnectionConfiguration connectionConfiguration = null;

        if (server.length() == 0 && proxyInfo == null)
        {
            connectionConfiguration = new ConnectionConfiguration(domain);
        }
        else if (server.length() == 0 && proxyInfo != null)
        {
            connectionConfiguration = new ConnectionConfiguration(domain,
                    proxyInfo);
        }
        else if (server.length() != 0 && proxyInfo == null)
        {
            connectionConfiguration = new ConnectionConfiguration(server, port,
                    domain);
        }
        else
        {
            connectionConfiguration = new ConnectionConfiguration(server, port,
                    domain, proxyInfo);
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

    /**
     * Returns the Eclipse {@linkplain ProxyInfo proxy information} for the
     * given host or <code>null</code> if it is not available
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ProxyInfo getProxyInfo(String host)
    {

        URI hostURI;

        try
        {
            hostURI = new URI(host);
        }
        catch (URISyntaxException e)
        {
            return null;
        }

//        BundleContext bundleContext = getBundle().getBundleContext();
//
//        ServiceReference serviceReference = bundleContext
//                .getServiceReference(IProxyService.class.getName());
//
//        IProxyService proxyService = (IProxyService) bundleContext
//                .getService(serviceReference);
//
//        if (proxyService == null || !proxyService.isProxiesEnabled())
//            return null;
//
//        for (IProxyData pd : proxyService.select(hostURI)) {
//            if (IProxyData.SOCKS_PROXY_TYPE.equals(pd.getType())) {
//                return ProxyInfo.forSocks5Proxy(pd.getHost(), pd.getPort(),
//                        pd.getUserId(), pd.getPassword());
//            }
//        }

        return null;
    }


    public boolean configureXMPPAccount()
    {
//        if (xmppAccountStore.isEmpty())
//            return (WizardUtils.openSarosConfigurationWizard() != null);
//
//        return (WizardUtils.openEditXMPPAccountWizard(xmppAccountStore
//                .getActiveAccount()) != null);

        return true;
    }

    public IWorkspace getWorkspace()
    {

        if(bTesting)
        {
            log.warn("Project is null - loading FS Workspace");
            return Workspace.instance();
        }
        else
        {
            log.info("Project loading IntelliJ Workspace");
            return WorkspaceIntl.instance();
        }

    }

}
