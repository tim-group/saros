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

package de.fu_berlin.inf.dpp.intellij.editor.internal;

import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IFileEditorInput;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.TextFileDocumentProvider;
import org.apache.log4j.Logger;

import org.picocontainer.annotations.Inject;


import de.fu_berlin.inf.dpp.annotations.Component;

import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * This Document provider tries tell others that files are not editable if
 * {@link Permission#READONLY_ACCESS}.
 */
@Component(module = "util")
public class SharedDocumentProvider extends TextFileDocumentProvider
{

    private static final Logger log = Logger
            .getLogger(SharedDocumentProvider.class.getName());

    protected ISarosSession sarosSession;

    @Inject
    protected ISarosSessionManager sessionManager;

    protected boolean hasWriteAccess;

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession = newSarosSession;
            hasWriteAccess = sarosSession.hasWriteAccess();
            sarosSession.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            assert sarosSession == oldSarosSession;
            sarosSession.removeListener(sharedProjectListener);
            sarosSession = null;
        }
    };

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void permissionChanged(User user) {
            if (sarosSession != null) {
                hasWriteAccess = sarosSession.hasWriteAccess();
            } else {
                log.warn("Internal error: Shared project null in permissionChanged!");
            }
        }
    };

    public SharedDocumentProvider(ISarosSessionManager sessionManager) {
        this.sessionManager = sessionManager;

        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
        sessionManager.addSarosSessionListener(sessionListener);
    }

    /**
     * This constructor is necessary when Eclipse creates a
     * SharedDocumentProvider.
     */
    public SharedDocumentProvider() {

        log.debug("SharedDocumentProvider created by Eclipse");

        SarosPluginContext.reinject(this);

        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
        sessionManager.addSarosSessionListener(sessionListener);
    }

    @Override
    public boolean isReadOnly(Object element) {
        return super.isReadOnly(element);
    }

    @Override
    public boolean isModifiable(Object element) {
        if (!isInSharedProject(element)) {
            return super.isModifiable(element);
        }

        return this.hasWriteAccess && super.isModifiable(element);
    }

    @Override
    public boolean canSaveDocument(Object element) {
        return super.canSaveDocument(element);
    }

    @Override
    public boolean mustSaveDocument(Object element) {
        return super.mustSaveDocument(element);
    }

    private boolean isInSharedProject(Object element) {

        if (sarosSession == null)
            return false;

        IFileEditorInput fileEditorInput = (IFileEditorInput) element;

        return sarosSession.isShared(ResourceAdapterFactory
                .create(fileEditorInput.getFile().getProject()));
    }
}