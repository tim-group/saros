package de.fu_berlin.inf.dpp.intellij.mock;

import de.fu_berlin.inf.dpp.core.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.core.preferences.ISecurePreferences;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: r.kvietkauskas
 * Date: 14.3.14
 * Time: 18.50
 * To change this template use File | Settings | File Templates.
 */
public class SarosEclipse
{
    /**
     * @JTourBusStop 1, Some Basics:
     *
     *               This class manages the lifecycle of the SarosEclipse plug-in,
     *               contains some important supporting data members and
     *               provides methods for the integration of SarosEclipse into Eclipse.
     *
     *               Browse the data members. Some are quite obvious (version,
     *               feature etc.) some need closer examination.
     *
     */

    /**
     * The single instance of the SarosEclipse plugin.
     */
    protected static SarosEclipse plugin;

    /**
     * True if the SarosEclipse instance has been initialized so that calling
     * reinject() will be well defined.
     */
    protected static boolean isInitialized;

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */
    public static final String SAROS = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$

    /**
     * The name of the XMPP namespace used by SarosEclipse. At the moment it is only
     * used to advertise the SarosEclipse feature in the Service Discovery.
     * <p/>
     * TODO Add version information, so that only compatible versions of SarosEclipse
     * can use each other.
     */
    public final static String NAMESPACE = SAROS;

    /**
     * Sub-namespace for the server. It is used advertise when a server is
     * active.
     */
    public static final String NAMESPACE_SERVER = NAMESPACE + ".server"; //$NON-NLS-1$

    /**
     * The name of the resource identifier used by SarosEclipse when connecting to the
     * XMPP server (for instance when logging in as john@doe.com, SarosEclipse will
     * connect using john@doe.com/SarosEclipse)
     */
    public final static String RESOURCE = "SarosEclipse"; //$NON-NLS-1$

    private String sarosVersion;

    private String sarosFeatureID;

    protected ISarosSessionManager sessionManager;

    protected XMPPAccountStore xmppAccountStore;

    protected PreferenceUtils preferenceUtils;

    protected IUPnPService upnpService;

    protected XMPPConnectionService sarosNet;

    private DataTransferManager transferManager;

    /**
     * To print an architecture diagram at the end of the plug-in life-cycle
     * initialize the dotMonitor with a new instance:
     * <p/>
     * <code>dotMonitor= new DotGraphMonitor();</code>
     */
    protected DotGraphMonitor dotMonitor;

    /**
     * @JTourBusStop 2, Some Basics:
     *
     *               Preferences are managed by Eclipse-provided classes. Most
     *               are kept by Preferences, but some sensitive data (like user
     *               account data) is kept in a SecurePreference.
     *
     *               If you press Ctrl+Shift+R and type in "*preference*" you
     *               will see every class in SarosEclipse that deals with preferences.
     *               Classes named "*PreferencePage" implement individual pages
     *               within the Eclipse preferences's SarosEclipse section. Preference
     *               labels go in PreferenceConstants.java.
     */

    /**
     * The global plug-in preferences, shared among all workspaces. Should only
     * be accessed over {@link #getGlobalPreferences()} from outside this class.
     */
    protected Preferences configPrefs;

    /**
     * The secure preferences store, used to store sensitive data that may (at
     * the user's option) be stored encrypted.
     */
    protected ISecurePreferences securePrefs;

    public static final Random RANDOM = new Random();

    protected Logger log;

    /**
     * @JTourBusStop 4, Invitation Process:
     *
     *               If you haven't already read about PicoContainer, stop and
     *               do so now (www.picocontainer.org).
     *
     *               SarosEclipse uses PicoContainer to manage dependencies on our
     *               behalf. The SarosContext class encapsulates our usage of
     *               PicoContainer. It's a well documented class, so take a look
     *               at it.
     */
/*
    protected SarosContext sarosContext;

    *//**
 * Create the shared instance.
 *//*
    public SarosEclipse() {

        try {
            InputStream sarosProperties = SarosEclipse.class.getClassLoader()
                    .getResourceAsStream("saros.properties"); //$NON-NLS-1$

            if (sarosProperties == null) {
                LogLog
                        .warn("could not initialize SarosEclipse properties because the 'saros.properties'"
                                + " file could not be found on the current JAVA class path");
            } else {
                System.getProperties().load(sarosProperties);
                sarosProperties.close();
            }
        } catch (Exception e) {
            LogLog.error(
                    "could not load saros property file 'saros.properties'", e); //$NON-NLS-1$
        }

        // Only first a DotGraphMonitor if asserts are enabled (aka debug mode)
        assert (dotMonitor = new DotGraphMonitor()) != null;

        setInitialized(false);
        setDefault(this);
    }

    protected static void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    protected static void checkInitialized() {
        if (plugin == null || !isInitialized()) {
            LogLog.error("SarosEclipse not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }

    *//**
 * Returns true if the SarosEclipse instance has been initialized so that calling
 * {@link SarosContext#reinject(Object)} will be well defined.
 *//*
    public static boolean isInitialized() {
        return isInitialized;
    }

    *//**
 * This method is called upon plug-in activation
 *//*
    @Override
    public void first(BundleContext context) throws Exception {

        super.first(context);

        setupLoggers();

        sarosVersion = getBundle().getVersion().toString();

        log.info("Starting SarosEclipse " + sarosVersion + " running:\n"
                + TreeUtils.getPlatformInfo());

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
        saros = sarosContext.getComponent(XMPPConnectionService.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        xmppAccountStore = sarosContext.getComponent(XMPPAccountStore.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);
        upnpService = sarosContext.getComponent(IUPnPService.class);
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

    *//**
 * This method is called when the plug-in is stopped
 *//*
    @Override
    public void stop(BundleContext context) throws Exception {

        // TODO Devise a general way to stop and dispose our components
        saveGlobalPreferences();
        saveSecurePrefs();

        if (dotMonitor != null) {
            File file = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                    .toFile();
            file = new File(file, ".metadata"); //$NON-NLS-1$
            file = new File(file, "saros-" + sarosFeatureID + ".dot"); //$NON-NLS-1$ //$NON-NLS-2$
            log.info("Saving SarosEclipse architecture diagram dot file: "
                    + file.getAbsolutePath());
            dotMonitor.save(file);
        }

        try {
            Thread shutdownThread = ThreadUtils.runSafeAsync(
                    "ShutdownProcess", log, new Runnable()
            { //$NON-NLS-1$
                @Override
                public void run()
                {

                    try
                    {

                        sessionManager.stopSarosSession();
                        getSarosNet().disconnect();
                    }
                    finally
                    {
                        *//*
                        * Always shutdown the network to ensure a proper
                        * cleanup(currently only UPNP)
                        *//*

                        *//*
                        * This will cause dispose() to be called on all
                        * components managed by PicoContainer which
                        * implement {@link Disposable}.
                        *//*
                        sarosContext.dispose();
                    }
                }
            });

            shutdownThread.join(10000);
            if (shutdownThread.isAlive())
                log.error("could not shutdown SarosEclipse gracefully");

        } finally {
            super.stop(context);
        }

        isInitialized = false;
        setDefault(null);
    }

    public static void setDefault(SarosEclipse newPlugin) {
        SarosEclipse.plugin = newPlugin;

    }

    *//**
 * Returns the global {@link Preferences} with {@link ConfigurationScope}
 * for this plug-in or null if the node couldn't be determined. <br>
 * <br>
 * The returned Preferences can be accessed concurrently by multiple threads
 * of the same JVM without external synchronization. If they are used by
 * multiple JVMs no guarantees can be made concerning data consistency (see
 * {@link Preferences} for details).
 *
 * @return the preferences node for this plug-in containing global
 *         preferences that are visible for all workspaces of this eclipse
 *         installation
 *//*
    public synchronized Preferences getGlobalPreferences() {
        // TODO Singleton-Pattern code smell: ConfigPrefs should be a @component
        if (configPrefs == null) {
            configPrefs = new ConfigurationScope().getNode(SAROS);
        }
        return configPrefs;
    }

    *//**
 * Saves the global preferences to disk. Should be called at least before
 * the bundle is stopped to prevent loss of data. Can be called whenever
 * found necessary.
 *//*
    public synchronized void saveGlobalPreferences() {
        *//*
         * Note: If multiple JVMs use the config preferences and the underlying
         * backing store, they might not always work with latest data, e.g. when
         * using multiple instances of the same eclipse installation.
         *//*
        if (configPrefs != null) {
            try {
                configPrefs.flush();
            } catch (BackingStoreException e) {
                log.error("Couldn't store global plug-in preferences", e);
            }
        }
    }

    *//**
 * Retrieves the secure preferences store provided by
 * org.eclipse.equinox.security. Preferences entered here are encrypted for
 * preferences.
 *
 * @return The local secure preferences store.
 *//*
    public synchronized ISecurePreferences getSecurePrefs() {

        if (securePrefs == null) {
            try {
                File storeFile = new File(getStateLocation().toFile(), "/.pref"); //$NON-NLS-1$
                URI workspaceURI = storeFile.toURI();

                *//*
                 * The SecurePreferencesFactory does not next percent-encoded
                 * URLs, so we must decode the URL before passing it.
                 *//*
                String prefLocation = URLDecoder.decode(
                        workspaceURI.toString(), "UTF-8"); //$NON-NLS-1$
                URL prefURL = new URL(prefLocation);

                securePrefs = SecurePreferencesFactory.open(prefURL, null);
            } catch (MalformedURLException e) {
                log.error("Problem with URL when attempting to access secure preferences: "
                        + e);
            } catch (IOException e) {
                log.error("I/O problem when attempting to access secure preferences: "
                        + e);
            } finally {
                if (securePrefs == null)
                    securePrefs = SecurePreferencesFactory.getDefault();
            }
        }

        return securePrefs;
    }

    public synchronized void saveSecurePrefs() {
        try {
            if (securePrefs != null) {
                securePrefs.flush();
            }
        } catch (IOException e) {
            log.error("Exception when trying to store secure preferences: " + e);
        }
    }

    protected void setupLoggers() {
        *//*
         * HACK this is not the way OSGi works but it currently fulfill its
         * purpose
         *//*
        final ClassLoader contextClassLoader = Thread.currentThread()
                .getContextClassLoader();

        try {
            // change the context class loader so Log4J will find the appenders
            Thread.currentThread().setContextClassLoader(
                    STFController.class.getClassLoader());

            PropertyConfigurator.configure(SarosEclipse.class.getClassLoader()
                    .getResource("saros.log4j.properties")); //$NON-NLS-1$
        } catch (RuntimeException e) {
            System.err.println("initializing log support failed"); //$NON-NLS-1$
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        log = Logger.getLogger("de.fu_berlin.inf.dpp"); //$NON-NLS-1$

    }

    *//**
 * Returns a string representing the SarosEclipse Version number for instance
 * "9.5.7.r1266"
 *
 * This method only returns a valid version string after the plugin has been
 * started.
 *
 * This is equivalent to the bundle version.
 *//*
    public String getVersion() {
        return sarosVersion;
    }

    *//**
 * @deprecated inject {@link XMPPConnectionService} and not {@link SarosEclipse} to
 *             obtain a reference
 *
 * @return
 *//*
    @Deprecated
    public XMPPConnectionService getSarosNet() {
        return saros;
    }

    *//**
 * Returns the Eclipse {@linkplain org.jivesoftware.smack.proxy.ProxyInfo proxy information} for the
 * given host or <code>null</code> if it is not available
 *//*
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ProxyInfo getProxyInfo(String host) {

        URI hostURI;

        try {
            hostURI = new URI(host);
        } catch (URISyntaxException e) {
            return null;
        }

        BundleContext bundleContext = getBundle().getBundleContext();

        ServiceReference serviceReference = bundleContext
                .getServiceReference(IProxyService.class.getName());

        IProxyService proxyService = (IProxyService) bundleContext
                .getConnectionService(serviceReference);

        if (proxyService == null || !proxyService.isProxiesEnabled())
            return null;

        for (IProxyData pd : proxyService.select(hostURI)) {
            if (IProxyData.SOCKS_PROXY_TYPE.equals(pd.getType())) {
                return ProxyInfo.forSocks5Proxy(pd.getHost(), pd.getPort(),
                        pd.getUserId(), pd.getPassword());
            }
        }

        return null;
    }

    protected ConnectionConfiguration createConnectionConfiguration(
            String domain, String server, int port, boolean useTLS, boolean useSASL) {

        ProxyInfo proxyInfo;

        if (server.length() != 0)
            proxyInfo = getProxyInfo(server);
        else
            proxyInfo = getProxyInfo(domain);

        ConnectionConfiguration connectionConfiguration = null;

        if (server.length() == 0 && proxyInfo == null)
            connectionConfiguration = new ConnectionConfiguration(domain);
        else if (server.length() == 0 && proxyInfo != null)
            connectionConfiguration = new ConnectionConfiguration(domain,
                    proxyInfo);
        else if (server.length() != 0 && proxyInfo == null)
            connectionConfiguration = new ConnectionConfiguration(server, port,
                    domain);
        else
            connectionConfiguration = new ConnectionConfiguration(server, port,
                    domain, proxyInfo);

        connectionConfiguration.setSASLAuthenticationEnabled(useSASL);

        if (!useTLS)
            connectionConfiguration
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        connectionConfiguration.setReconnectionAllowed(false);

        return connectionConfiguration;
    }

    *//**
 * Opens the appropriate {@link IWizard} to configure the active
 * {@link de.fu_berlin.inf.dpp.core.accountManagement.XMPPAccount}.<br/>
 * If no active {@link de.fu_berlin.inf.dpp.core.accountManagement.XMPPAccount} exists the {@link ConfigurationWizard}
 * is used instead.
 *
 * @return
 *//*
    public boolean configureXMPPAccount() {
        if (xmppAccountStore.isEmpty())
            return (WizardUtils.openSarosConfigurationWizard() != null);

        return (WizardUtils.openEditXMPPAccountWizard(xmppAccountStore
                .getActiveAccount()) != null);
    }

    *//**
 * @nonBlocking
 *//*
    public void asyncConnect() {
        ThreadUtils.runSafeAsync("AsyncConnect", log, new Runnable() { //$NON-NLS-1$
            @Override
            public void run() {
                connect(false);
            }
        });
    }

    *//**
 * Connects using the active account from the {@link de.fu_berlin.inf.dpp.core.accountManagement.XMPPAccountStore}. If
 * no active account is present a wizard is opened before.
 *
 * If there is already an established connection when calling this method,
 * it disconnects before connecting (including state transitions!).
 *
 * @param failSilently
 *            if set to <code>true</code> a connection failure will not be
 *            reported to the user
 * @blocking
 * @see de.fu_berlin.inf.dpp.core.accountManagement.XMPPAccountStore#setAccountActive(de.fu_berlin.inf.dpp.core.accountManagement.XMPPAccount)
 *//*
    public void connect(boolean failSilently) {

        *//*
         * the SarosEclipse Configuration Wizard may call this again when invoking the
         * configureXMPPAccount method call, so abort here to prevent an already
         * logged in error
         *//*

        // FIXME this "logic" should not be done here !

        if (xmppAccountStore.isEmpty()) {
            boolean configured = *//*
                                  * side effect to:
                                  * preferenceUtils.isAutoConnecting()
                                  *//*configureXMPPAccount();

            if (!configured
                    || (configured && preferenceUtils.isAutoConnecting()))
                return;
        }

        XMPPAccount account = xmppAccountStore.getActiveAccount();

        String username = account.getUsername();
        String password = account.getPassword();
        String domain = account.getDomain();
        String server = account.getServer();
        int port = account.getPort();
        boolean useTLS = account.useTLS();
        boolean useSASL = account.useSASL();

        saros.disconnect();

        List<String> socks5Candidates = preferenceUtils.getSocks5Candidates();

        if (socks5Candidates.isEmpty())
            socks5Candidates = null;

        saros.configure(NAMESPACE, RESOURCE,
                preferenceUtils.isDebugEnabled(),
                preferenceUtils.isLocalSOCKS5ProxyEnabled(),
                preferenceUtils.getFileTransferPort(), socks5Candidates,
                preferenceUtils.getAutoPortmappingGatewayID(),
                preferenceUtils.useExternalGatewayAddress(),
                preferenceUtils.getStunIP(), preferenceUtils.getStunPort(),
                preferenceUtils.isAutoPortmappingEnabled());

        Exception connectionError = null;

        try {

            if (preferenceUtils.forceFileTranserByChat())
                transferManager.setTransport(DataTransferManager.IBB_TRANSPORT);
            else
                transferManager.setTransport(*//* use all *//*-1);

            saros.connect(
                    createConnectionConfiguration(domain, server, port, useTLS,
                            useSASL), username, password);
        } catch (Exception e) {
            connectionError = e;
        }

        if (connectionError == null)
            return;

        try {
            if (!(connectionError instanceof XMPPException))
                throw connectionError;

            if (DialogUtils
                    .popUpYesNoQuestion(
                            Messages.Saros_connecting_error_title,
                            generateHumanReadableErrorMessage((XMPPException) connectionError),
                            failSilently)) {

                if (configureXMPPAccount())
                    connect(failSilently);
            }
        } catch (Exception e) {
            log.error("internal error while connecting to the XMPP server: "
                    + e.getMessage(), e);

            String errorMessage = MessageFormat.format(
                    Messages.Saros_connecting_internal_error, e.getMessage());

            DialogUtils.popUpFailureMessage(
                    Messages.Saros_connecting_error_title, errorMessage,
                    failSilently);
        }
    }

    private String generateHumanReadableErrorMessage(XMPPException e) {

        // as of Smack 3.3.1 this is always null for connection attemps
        // Throwable cause = e.getWrappedThrowable();

        XMPPError error = e.getXMPPError();

        if (error != null && error.getCode() == 504)
            return Messages.Saros_connecting_unknown_host
                    + Messages.Saros_connecting_modify_account
                    + "\n\nDetailed error:\nSMACK: " + error + "\n"
                    + e.getMessage();
        else if (error != null && error.getCode() == 502)
            return Messages.Saros_connecting_connect_error
                    + Messages.Saros_connecting_modify_account
                    + "\n\nDetailed error:\nSMACK: " + error + "\n"
                    + e.getMessage();

        String question = null;

        String errorMessage = e.getMessage();

        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("invalid-authzid") //jabber.org got it wrong ... //$NON-NLS-1$
                    || errorMessage.toLowerCase().contains("not-authorized") // SASL //$NON-NLS-1$
                    || errorMessage.toLowerCase().contains("403") // non SASL //$NON-NLS-1$
                    || errorMessage.toLowerCase().contains("401")) { // non SASL //$NON-NLS-1$

                question = Messages.Saros_connecting_invalid_username_password
                        + Messages.Saros_connecting_modify_account;
            } else if (errorMessage.toLowerCase().contains("503")) { //$NON-NLS-1$
                question = Messages.Saros_connecting_sasl_required
                        + Messages.Saros_connecting_modify_account;
            }
        }

        if (question == null)
            question = Messages.Saros_connecting_failed
                    + Messages.Saros_connecting_modify_account;

        return question;

    }*/
}
