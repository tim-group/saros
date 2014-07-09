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

package de.fu_berlin.inf.dpp.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import de.fu_berlin.inf.dpp.core.context.SarosContext;
import de.fu_berlin.inf.dpp.core.context.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.context.SarosIntellijContextFactory;
import de.fu_berlin.inf.dpp.intellij.store.PreferenceStore;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosMainPanelView;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

import java.util.Random;

/**
 * Saros plugin class
 */
public class Saros {

    protected static Logger LOG = Logger.getLogger(Saros.class);

    public static Random RANDOM = new Random();

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */

    public static final String SAROS = "de.fu_berlin.inf.dpp";

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
    public final static String RESOURCE = "Saros";

    /**
     * Sub-namespace for the server. It is used advertise when a server is
     * active.
     */
    public static final String NAMESPACE_SERVER =
            NAMESPACE + ".server";

    private static Saros _instance;

    private static boolean isInitialized;

    private Project project;
    private ToolWindow toolWindow;

    private XMPPConnectionService connectionService;
    private ISarosSessionManager sessionManager;
    protected PreferenceUtils preferenceUtils;

    private SarosMainPanelView mainPanel;
    private IWorkspace workspace;

    private SarosContext sarosContext;

    protected IPreferenceStore preferenceStore;

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link de.fu_berlin.inf.dpp.core.context.SarosContext#reinject(Object)} will be well defined.
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void checkInitialized() {
        if (!isInitialized()) {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }

    public SarosMainPanelView getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(SarosMainPanelView mainPanel) {
        this.mainPanel = mainPanel;
    }

    public static Saros create(Project project, ToolWindow toolWindow) {
        if (_instance == null) {
            _instance = new Saros(project, toolWindow);
        }
        return _instance;
    }

    /**
     * Instance of Saros
     *
     * @return
     */
    public static Saros instance() {
        if (_instance == null) {
            throw new Error("Saros not initialized");
        }
        return _instance;
    }

    private Saros(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
    }

    /**
     * If not initizalied yet, this method initializes fields, the SarosPluginContext and the XMPPConnectionService.
     */
    public void start() {

        if (isInitialized) {
            return;
        }

        this.preferenceStore = new PreferenceStore();

        //CONTEXT
        this.sarosContext = new SarosContext(
                new SarosIntellijContextFactory(this,
                        new SarosCoreContextFactory()), new DotGraphMonitor()
        );

        SarosPluginContext.setSarosContext(sarosContext);

        connectionService = sarosContext
                .getComponent(XMPPConnectionService.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);

        //todo: set parameters from config
        connectionService
                .configure(Saros.NAMESPACE, Saros.RESOURCE, false, false, 8888,
                        null, null, true, null, 80, true);

        this.isInitialized = true;
        // Make sure that all components in the container are
        // instantiated
        this.sarosContext.getComponents(Object.class);

    }

    public void stop() {
        this.isInitialized = false;
    }

    public boolean isConnected() {
        return connectionService.isConnected();
    }

    public XMPPConnectionService getConnectionService() {
        return connectionService;
    }

    //TODO: Check if this can be replaced by injection
    public ISarosSessionManager getSessionManager() {
        return sessionManager;
    }

    public IPreferenceStore getPreferenceStore() {
        return preferenceStore;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    public SarosContext getSarosContext() {
        return sarosContext;
    }

    public IWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(IWorkspace workspace) {
        this.workspace = workspace;
    }
}
