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

package de.fu_berlin.inf.dpp.intellij.context;

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.ISarosContextFactory;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.concurrent.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.core.editor.EditorManager;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.core.ui.eventhandler.*;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFileContentChangedNotifier;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.intellij.editor.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileContentChangedNotifierBridge;
import de.fu_berlin.inf.dpp.intellij.runtime.IntelliJSynchronizer;
import de.fu_berlin.inf.dpp.intellij.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import java.util.Arrays;

/**
 *
 */
//todo: adopted from eclipse
public class SarosIntellijContextFactory extends AbstractSarosContextFactory {


    private final ISarosContextFactory additionalContext;

    private final Saros saros;

    private final Component[] components = new Component[]{


            Component.create(ISarosSessionManager.class, SarosSessionManager.class),
            //TODO: Not necessary
            //Component.create(NotificationHandler.class),
            // Core Managers
            Component.create(ConsistencyWatchdogClient.class),

            Component.create(EditorAPI.class),

            Component.create(EditorManager.class),

            // UI handlers
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
            Component.create(IChecksumCache.class, ChecksumCacheImpl.class),  //todo
//            Component.create(IChecksumCache.class, new ChecksumCacheImpl(
//                    new FileContentNotifierBridge())),


            // SWT EDT support
            Component.create(UISynchronizer.class, IntelliJSynchronizer.class),

            Component.create(IFileContentChangedNotifier.class, FileContentChangedNotifierBridge.class),


            Component.create(PreferenceUtils.class),

            Component.create(FollowModeAction.class),
            Component.create(LeaveSessionAction.class),

            //   Component.create(IAddProjectToSessionWizard.class, AddProjectToSessionWizard.class),


    };

    public SarosIntellijContextFactory(Saros saros, ISarosContextFactory delegate) {
        this.saros = saros;
        this.additionalContext = delegate;
    }


    @Override
    public void createComponents(MutablePicoContainer container) {

        IWorkspace workspace = saros.getWorkspace();
        FileUtils.workspace = workspace;

        // container.addComponent(Saros.class,saros);
        container.addComponent(IPreferenceStore.class, saros.getPreferenceStore());

        // Saros Core PathIntl Support
        container.addComponent(IPathFactory.class, workspace.getPathFactory());


        container.addComponent(IWorkspace.class, workspace);
        // container.addComponent(Workspace.class, workspace);

        if (additionalContext != null) {
            additionalContext.createComponents(container);
        }

        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                    component.getImplementation());
        }

        container.addComponent(saros);

        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.SarosVersion.class), "14.1.31.DEVEL");  //todo


        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.PlatformVersion.class), "4.3.2"); //todo

    }
}
