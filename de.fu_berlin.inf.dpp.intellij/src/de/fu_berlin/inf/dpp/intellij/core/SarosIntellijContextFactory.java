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

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.ISarosContextFactory;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.intellij.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.ISecurePreferences;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.FileContentChangedNotifier;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.core.project.internal.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.core.project.internal.IFileContentChangedNotifier;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.intellij.concurrent.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.intellij.concurrent.undo.UndoManager;
import de.fu_berlin.inf.dpp.intellij.synchronize.IntelliJSynchronizer;
import de.fu_berlin.inf.dpp.intellij.editor.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.ui.LocalPresenceTracker;
import de.fu_berlin.inf.dpp.intellij.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SarosUI;
import de.fu_berlin.inf.dpp.intellij.ui.eventhandler.*;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.27
 * Time: 07.36
 */

public class SarosIntellijContextFactory extends AbstractSarosContextFactory
{



    private final ISarosContextFactory additionalContext;

    private final Saros saros;

    private final Component[] components = new Component[]{


            Component.create(SarosSessionManager.class),
            Component.create(ISarosSessionManager.class, SarosSessionManager.class),
            // Core Managers
            Component.create(ConsistencyWatchdogClient.class),

            //   Component.create(EditorAPI.class),
            Component.create(IEditorAPI.class, EditorAPI.class),
            //  Component.create(EditorManagerEcl.class),
           // Component.create(IEditorManager.class, EditorManagerEcl.class),

            Component.create(IEditorManager.class, EditorManager.class),
          //  Component.create(EditorManager.class),
            // disabled because of privacy violations
            // see
            // http://opus.haw-hamburg.de/volltexte/2011/1391/pdf/ba_krassmann_online.pdf
            // page 47
            Component.create(LocalPresenceTracker.class),


            Component.create(SarosUI.class),
            Component.create(SessionViewOpener.class),
            Component.create(UndoManager.class),


            Component.create(Container.class),

            // UI handlers
            Component.create(HostLeftAloneInSessionHandler.class),
            Component.create(NegotiationHandler.class),
            Component.create(UserStatusChangeHandler.class),
            Component.create(JoinSessionRequestHandler.class),
            Component.create(JoinSessionRejectedHandler.class),
            Component.create(ServerPreferenceHandler.class),
            Component.create(SessionStatusRequestHandler.class),
            Component.create(XMPPAuthorizationHandler.class),


            //   Component.create(UISynchronizer.class, UISynchronizerImpl.class),


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

            Component.create(IFileContentChangedNotifier.class, FileContentChangedNotifier.class),


            Component.create(PreferenceUtils.class),

            Component.create(FollowModeAction.class),
            Component.create(LeaveSessionAction.class),

            //   Component.create(IAddProjectToSessionWizard.class, AddProjectToSessionWizard.class),


    };

    public SarosIntellijContextFactory(Saros saros, ISarosContextFactory delegate)
    {
        this.saros = saros;
        this.additionalContext = delegate;
    }


    @Override
    public void createComponents(MutablePicoContainer container)
    {
        //IWorkspace workspace = Saros.instance().getWorkspace();
        IWorkspace workspace = saros.getWorkspace();
      //  FileList.workspace = workspace;
      //  FileListDiff.workspace = workspace;
        FileUtils.workspace = workspace;

       // container.addComponent(Saros.class,saros);
        container.addComponent(IPreferenceStore.class, saros.getConfigPrefs());
        container.addComponent(ISecurePreferences.class, saros.getSecurePrefs());

        // Saros Core PathIntl Support
        container.addComponent(IPathFactory.class, workspace.getPathFactory());


        container.addComponent(IWorkspace.class, workspace);
       // container.addComponent(Workspace.class, workspace);

        if (additionalContext != null)
        {
            additionalContext.createComponents(container);
        }

        for (Component component : Arrays.asList(components))
        {
            container.addComponent(component.getBindKey(),
                    component.getImplementation());
        }

        container.addComponent(saros);

        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.SarosVersion.class), "14.1.31.DEVEL");  //todo

//        container.addComponent(BindKey.bindKey(String.class,
//                ISarosContextBindings.SarosVersion.class), "13.12.6");


        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.PlatformVersion.class), "4.3.2"); //todo


        //  container.addComponent(Preferences.class, saros.getGlobalPreferences());
    }
}
