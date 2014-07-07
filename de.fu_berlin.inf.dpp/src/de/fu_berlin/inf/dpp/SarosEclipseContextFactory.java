package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.concurrent.undo.UndoManager;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.filesystem.EclipsePathFactory;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.project.internal.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.project.internal.FileContentNotifierBridge;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.synchronize.internal.SWTSynchronizer;
import de.fu_berlin.inf.dpp.ui.RemoteProgressManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.eventhandler.HostLeftAloneInSessionHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.JoinSessionRejectedHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.JoinSessionRequestHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.NegotiationHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.ServerPreferenceHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.SessionStatusRequestHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.SessionViewOpener;
import de.fu_berlin.inf.dpp.ui.eventhandler.UserStatusChangeHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.XMPPAuthorizationHandler;
import de.fu_berlin.inf.dpp.vcs.EclipseVCSProviderFactoryImpl;
import de.fu_berlin.inf.dpp.vcs.VCSProviderFactory;

/**
 * Factory used for creating the Saros context when running as Eclipse plugin.
 * 
 * * @author srossbach
 */

// TODO class is misplaced in the current package along with Saros Eclipse stuff
public class SarosEclipseContextFactory extends AbstractSarosContextFactory {

    private final ISarosContextFactory additionalContext;

    private final Saros saros;

    private final Component[] components = new Component[] {
        // Core Managers
        Component.create(ConsistencyWatchdogClient.class),
        Component.create(EditorAPI.class),
        Component.create(EditorManager.class),
        // disabled because of privacy violations
        // see
        // http://opus.haw-hamburg.de/volltexte/2011/1391/pdf/ba_krassmann_online.pdf
        // page 47
        // Component.create(LocalPresenceTracker.class),

        Component.create(PreferenceUtils.class),
        Component.create(SarosUI.class),
        Component.create(SessionViewOpener.class),
        Component.create(UndoManager.class),
        Component.create(RemoteProgressManager.class),

        // UI handlers
        Component.create(HostLeftAloneInSessionHandler.class),
        Component.create(NegotiationHandler.class),
        Component.create(UserStatusChangeHandler.class),
        Component.create(JoinSessionRequestHandler.class),
        Component.create(JoinSessionRejectedHandler.class),
        Component.create(ServerPreferenceHandler.class),
        Component.create(SessionStatusRequestHandler.class),
        Component.create(XMPPAuthorizationHandler.class),
        // Cache support
        /*
         * TODO avoid direct creation as this will become tricky especially if
         * we are the delegate and depends on components that are only available
         * after we added all our context stuff or vice versa
         */
        Component.create(IChecksumCache.class, new ChecksumCacheImpl(
            new FileContentNotifierBridge())),

        // Saros Core Path Support
        Component.create(IPathFactory.class, EclipsePathFactory.class),

        // SWT EDT support
        Component.create(UISynchronizer.class, SWTSynchronizer.class),

        // VCS (SVN only)
        Component.create(VCSProviderFactory.class,
            EclipseVCSProviderFactoryImpl.class) };

    public SarosEclipseContextFactory(Saros saros, ISarosContextFactory delegate) {
        this.saros = saros;
        this.additionalContext = delegate;
    }

    @Override
    public void createComponents(MutablePicoContainer container) {

        if (additionalContext != null)
            additionalContext.createComponents(container);

        for (Component component : Arrays.asList(components))
            container.addComponent(component.getBindKey(),
                component.getImplementation());

        container.addComponent(saros);

        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.SarosVersion.class), saros.getBundle()
            .getVersion().toString());

        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.PlatformVersion.class),
            Platform.getBundle("org.eclipse.core.runtime").getVersion()
                .toString());

        container.addComponent(IPreferenceStore.class,
            saros.getPreferenceStore());

        container
            .addComponent(ISecurePreferences.class, saros.getSecurePrefs());

        container.addComponent(Preferences.class, saros.getGlobalPreferences());
    }
}
