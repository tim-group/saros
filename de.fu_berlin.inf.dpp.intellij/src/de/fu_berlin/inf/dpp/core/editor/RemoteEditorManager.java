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

package de.fu_berlin.inf.dpp.core.editor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.activities.*;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.core.editor.text.LineRange;
import de.fu_berlin.inf.dpp.core.editor.text.TextSelection;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This class contains the state of the editors, viewports and selections of all
 * remote users as we believe it to be by listening to the Activities we
 * receive.
 */
public class RemoteEditorManager {

    private static final Logger log = Logger
            .getLogger(RemoteEditorManager.class);

    protected Map<User, RemoteEditorState> editorStates = new HashMap<User, RemoteEditorState>();

    protected ISarosSession sarosSession;

    /**
     * One editor of one user
     */
    public static class RemoteEditor {

        protected SPath path;

        protected ITextSelection selection;

        protected ILineRange viewport;

        public RemoteEditor(SPath path) {
            this.path = path;
        }

        public SPath getPath() {
            return path;
        }

        public ILineRange getViewport() {
            return viewport;
        }

        public void setViewport(ILineRange viewport) {
            this.viewport = viewport;
        }

        public void setSelection(ITextSelection selection) {
            this.selection = selection;
        }

        public ITextSelection getSelection() {
            return this.selection;
        }
    }

    /**
     * This class represents the state of the editors, viewports and selection
     * of a user.
     */
    public static class RemoteEditorState {

        protected User user;

        protected LinkedHashMap<SPath, RemoteEditor> openEditors = new LinkedHashMap<SPath, RemoteEditor>();

        protected RemoteEditor activeEditor;

        public RemoteEditorState(User user) {
            this.user = user;
        }

        public void setSelection(SPath path, ITextSelection selection) {

            if (!openEditors.containsKey(path)) {
                log.warn("received selection from user [" + this.user
                        + "] for editor which was never activated: " + path);
                return;
            }

            getRemoteEditor(path).setSelection(selection);

        }

        public void setViewport(SPath path, ILineRange viewport) {

            if (!openEditors.containsKey(path)) {
                log.warn("Viewport for editor which was never activated: "
                        + path);
                return;
            }

            getRemoteEditor(path).setViewport(viewport);

        }

        public void activated(SPath path) {
            if (path == null) {
                activeEditor = null;
            } else {
                activeEditor = openEditors.get(path);

                // Create new or reinsert at the end of the Map
                if (activeEditor == null) {
                    activeEditor = new RemoteEditor(path);
                } else {
                    openEditors.remove(path);
                }
                openEditors.put(path, activeEditor);

            }
        }

        /**
         * Returns a RemoteEditor representing the given path for the given
         * user.
         *
         * This method never returns null but creates a new RemoteEditor lazily.
         *
         * To query whether the user of this RemoteEditorState has the editor of
         * the given path open use isRemoteOpenEditor().
         */
        public RemoteEditor getRemoteEditor(SPath path) {
            RemoteEditor result = openEditors.get(path);
            if (result == null) {
                result = new RemoteEditor(path);
                openEditors.put(path, result);
            }
            return result;
        }

        public RemoteEditor getLastEditor() {
            RemoteEditor editor = null;
            Iterator<RemoteEditor> it = openEditors.values().iterator();
            while (it.hasNext()) {
                editor = it.next();
            }
            return editor;
        }

        public void closed(SPath path) {

            RemoteEditor remoteEditor = openEditors.remove(path);

            if (remoteEditor == null) {
                log.warn("Removing an editor which has never been added: "
                        + path);
                return;
            }

            if (remoteEditor == activeEditor) {
                activeEditor = null;
            }
        }

        protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
            @Override
            public void receive(EditorActivity editorActivity) {

                SPath sPath = editorActivity.getPath();

                switch (editorActivity.getType()) {
                    case ACTIVATED:
                        activated(sPath);
                        break;
                    case SAVED:
                        break;
                    case CLOSED:
                        closed(sPath);
                        break;
                    default:
                        log.warn("Unexpected type: " + editorActivity.getType());
                        assert false;
                }
            }

            @Override
            public void receive(ViewportActivity viewportActivity) {

                ILineRange lineRange = new LineRange(
                        viewportActivity.getStartLine(),
                        viewportActivity.getNumberOfLines());

                setViewport(viewportActivity.getPath(), lineRange);
            }

            @Override
            public void receive(TextSelectionActivity textSelectionActivity) {

                ITextSelection selection = new TextSelection(
                        textSelectionActivity.getOffset(),
                        textSelectionActivity.getLength());

                setSelection(textSelectionActivity.getPath(), selection);
            }
        };

        /**
         * Returns the activeEditor of the user of this RemoteEditorState or
         * null if the user has no editor open currently.
         */
        public RemoteEditor getActiveEditor() {
            return this.activeEditor;
        }

        public boolean isRemoteActiveEditor(SPath path) {
            if (activeEditor != null && activeEditor.getPath().equals(path))
                return true;
            return false;
        }

        public boolean isRemoteOpenEditor(SPath path) {
            return openEditors.containsKey(path);
        }

        public User getUser() {
            return this.user;
        }

        /**
         * Returns a snapshot copy of the editors open for the user represented
         * by this RemoteEditorState.
         */
        public Set<SPath> getRemoteOpenEditors() {
            return new HashSet<SPath>(openEditors.keySet());
        }

        public void exec(IActivity activity) {
            activity.dispatch(activityReceiver);
        }
    }

    public RemoteEditorManager(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    public RemoteEditorState getEditorState(User user) {

        RemoteEditorState result = editorStates.get(user);
        if (result == null) {
            result = new RemoteEditorState(user);
            editorStates.put(user, result);
        }
        return result;
    }

    public void exec(IActivity activity) {
        getEditorState(activity.getSource()).exec(activity);
    }

    /**
     * Returns the selection of the given user in the currently active editor or
     * null if the user has no active editor or no selection in the active
     * editor.
     */
    public ITextSelection getSelection(User user) {

        RemoteEditor activeEditor = getEditorState(user).getActiveEditor();

        if (activeEditor == null)
            return null;

        return activeEditor.getSelection();
    }

    /**
     * @return the viewport of the given user in the currently active editor or
     *         <code>null</code> if the user has no active editor.
     */
    public ILineRange getViewport(User user) {
        if (sarosSession.getLocalUser().equals(user)) {
            throw new IllegalArgumentException(
                    "Viewport of the local user was queried.");
        }

        RemoteEditor activeEditor = getEditorState(user).getActiveEditor();

        if (activeEditor == null)
            return null;

        return activeEditor.getViewport();
    }

    /**
     * Clears all state information associated with the given user.
     */
    public void removeUser(User participant) {
        editorStates.remove(participant);
    }

    public List<User> getRemoteOpenEditorUsers(SPath path) {
        ArrayList<User> result = new ArrayList<User>();
        for (RemoteEditorState state : editorStates.values()) {
            if (state.isRemoteOpenEditor(path))
                result.add(state.getUser());
        }
        return result;
    }

    public List<User> getRemoteActiveEditorUsers(SPath path) {

        ArrayList<User> result = new ArrayList<User>();

        for (RemoteEditorState state : editorStates.values()) {
            if (state.isRemoteActiveEditor(path)) {
                result.add(state.getUser());
            }
        }
        return result;
    }

    /**
     * Returns a set of all paths representing the editors which are currently
     * opened by the remote users of this shared session (i.e. not our own).
     *
     * If no editors are opened an empty set is being returned.
     */
    public Set<SPath> getRemoteOpenEditors() {
        Set<SPath> result = new HashSet<SPath>();
        for (RemoteEditorState state : editorStates.values()) {
            result.addAll(state.openEditors.keySet());
        }
        return result;
    }

    /**
     * Returns a set of all paths representing the editors which are currently
     * opened and active by the remote users of this shared session (i.e. not
     * our own).
     *
     * @return set of all active remote editors
     */
    public Set<SPath> getRemoteActiveEditors() {
        Set<SPath> result = new HashSet<SPath>();
        for (RemoteEditorState state : editorStates.values()) {
            if (state.activeEditor != null)
                result.add(state.activeEditor.getPath());
        }
        return result;
    }

    /**
     * Returns a snapshot copy of all paths representing the editors which are
     * currently opened by the given user of this shared session (i.e. not our
     * own).
     *
     * If no editors are opened by the given user an empty set is being
     * returned.
     */
    public Set<SPath> getRemoteOpenEditors(User user) {
        return getEditorState(user).getRemoteOpenEditors();
    }

    /**
     * Returns the active Editor which is currently open by the given user of
     * this shared session (i.e. not our own).
     *
     * @return the active RemoteEditor or <code>null</code> if the given user
     *         has no editor open.
     */
    public RemoteEditor getRemoteActiveEditor(User user) {
        return getEditorState(user).getActiveEditor();
    }

    /**
     * Checks if the active editor of the given user is part of the Saros
     * session, or not. Convenience method for calling
     * getEditorState(user).getActiveEditor() != null.
     *
     * @return <code>true</code>, if the currently active remote editor of the
     *         given user is shared via the Saros session, <code>false</code>
     *         otherwise.
     */
    public boolean isRemoteActiveEditorShared(User user) {
        return getRemoteActiveEditor(user) != null;
    }
}
