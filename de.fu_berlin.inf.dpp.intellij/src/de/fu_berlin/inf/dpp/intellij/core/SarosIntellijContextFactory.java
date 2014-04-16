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

import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.core.context.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.core.context.ISarosContextFactory;
import de.fu_berlin.inf.dpp.core.editor.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.core.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.ISecurePreferences;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.FileContentChangedNotifier;
import de.fu_berlin.inf.dpp.core.project.IChecksumCache;
import de.fu_berlin.inf.dpp.core.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.core.project.internal.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.core.project.internal.IFileContentChangedNotifier;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.intellij.concurrent.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.intellij.concurrent.ConsistencyWatchdogHandler;
import de.fu_berlin.inf.dpp.intellij.concurrent.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.intellij.core.misc.UISynchronizerImpl;
import de.fu_berlin.inf.dpp.intellij.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.project.PathFactory;
import de.fu_berlin.inf.dpp.intellij.project.Workspace;
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





            Component.create(SarosSessionManager.class),  //todo ???

            // Core Managers
               Component.create(ConsistencyWatchdogClient.class),

            //   Component.create(EditorAPI.class),
            Component.create(IEditorAPI.class, EditorAPI.class),
            //  Component.create(EditorManager.class),
            Component.create(IEditorManager.class, EditorManager.class),
            // disabled because of privacy violations
            // see
            // http://opus.haw-hamburg.de/volltexte/2011/1391/pdf/ba_krassmann_online.pdf
            // page 47
            // Component.create(LocalPresenceTracker.class),


//            Component.create(SarosUI.class),
//            Component.create(SessionViewOpener.class),
//            Component.create(UndoManager.class),


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


            Component.create(UISynchronizer.class, UISynchronizerImpl.class),
            Component.create(IPathFactory.class, PathFactory.class),

            Component.create(ColorIDSetStorage.class),

            // Cache support
            /*
            * TODO avoid direct creation as this will become tricky especially if
            * we are the delegate and depends on components that are only available
            * after we added all our context stuff or vice versa
            */
            Component.create(IChecksumCache.class, ChecksumCacheImpl.class),  //todo
//            Component.create(IChecksumCache.class, new ChecksumCacheImpl(
//                    new FileContentNotifierBridge())),

            // Saros Core PathImp Support
            //    Component.create(IPathFactory.class, EclipsePathFactory.class),

            // SWT EDT support
            //       Component.create(UISynchronizer.class, SWTSynchronizer.class)

            Component.create(IFileContentChangedNotifier.class, FileContentChangedNotifier.class),


            //Component.create(IWorkspace.class, Workspace.class),

            Component.create(IsInconsistentObservable.class),

            Component.create(PreferenceUtils.class),

            //  Component.create(IAddProjectToSessionWizard.class, AddProjectToSessionWizard.class),


    };

    public SarosIntellijContextFactory(Saros saros, ISarosContextFactory delegate)
    {
        this.saros = saros;
        this.additionalContext = delegate;
    }

    @Override
    public void createComponents(MutablePicoContainer container)
    {

        container.addComponent(IPreferenceStore.class, saros.getConfigPrefs());
        container.addComponent(ISecurePreferences.class, saros.getSecurePrefs());
        container.addComponent(IWorkspace.class, Workspace.instance());

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
