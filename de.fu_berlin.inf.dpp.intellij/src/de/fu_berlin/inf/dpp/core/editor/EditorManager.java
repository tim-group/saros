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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.activities.*;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.EditorActionManager;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorManager;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
import de.fu_berlin.inf.dpp.intellij.editor.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationHandler;
import de.fu_berlin.inf.dpp.session.*;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * IntelliJ implementation of editor manager
 */
public class EditorManager
        extends AbstractActivityProducer {

    protected static final Logger LOG = Logger.getLogger(EditorManager.class);

    protected SharedEditorListenerDispatch editorListenerDispatch = new SharedEditorListenerDispatch();

    protected final EditorActionManager actionManager = new EditorActionManager(this);

    protected final IPreferenceStore preferenceStore;

    protected RemoteEditorManager remoteEditorManager;

    protected RemoteWriteAccessManager remoteWriteAccessManager;

    protected ISarosSession session;

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    protected User followedUser = null;

    protected boolean hasWriteAccess;

    protected boolean isLocked;


    protected Set<SPath> locallyOpenEditors = new HashSet<SPath>();

    protected SelectionEvent localSelection;
    protected LineRange localViewport;

    protected SPath activeEditor;

    private IActivityReceiver activityReceiver = new AbstractActivityReceiver() {

        @Override
        public void receive(EditorActivity editorActivity) {
            execEditorActivity(editorActivity);
        }


        @Override
        public void receive(TextEditActivity textEditActivity) {
            execTextEdit(textEditActivity);
        }


        @Override
        public void receive(TextSelectionActivity textSelectionActivity) {
            execTextSelection(textSelectionActivity);
        }


        @Override
        public void receive(ViewportActivity viewportActivity) {
            execViewport(viewportActivity);
        }
    };

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {

        @Override
        public void exec(IActivity activity) {

            User sender = activity.getSource();
            if (!sender.isInSession()) {
                LOG.warn("skipping execution of activity " + activity
                        + " for user " + sender
                        + " who is not in the current session");
                return;
            }

            // First let the remote managers update itself based on the
            // Activity
            remoteEditorManager.exec(activity);
            remoteWriteAccessManager.exec(activity);

            super.exec(activity);
        }

        @Override
        public void receive(EditorActivity editorActivity) {
            execEditorActivity(editorActivity);
        }

        @Override
        public void receive(TextEditActivity textEditActivity) {
            execTextEdit(textEditActivity);
        }

        @Override
        public void receive(TextSelectionActivity textSelectionActivity) {
            execTextSelection(textSelectionActivity);
        }

        @Override
        public void receive(ViewportActivity viewportActivity) {
            execViewport(viewportActivity);
        }
    };

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {


        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            LOG.info("Session started");

            assert getActionManager().getEditorPool().getEditors().size() == 0 : "EditorPool was not correctly reset!";

            session = newSarosSession;
            session.getStopManager().addBlockable(stopManagerListener);

            hasWriteAccess = session.hasWriteAccess();
            session.addListener(sharedProjectListener);

            session.addActivityProducer(EditorManager.this);
            session.addActivityConsumer(consumer);

            remoteEditorManager = new RemoteEditorManager(session);
            remoteWriteAccessManager = new RemoteWriteAccessManager(session);

            LocalFileSystem.getInstance().refresh(true);
        }


        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {

            LOG.info("Session ended");

            assert session == oldSarosSession;
            session.getStopManager().removeBlockable(stopManagerListener); //todo

            ThreadUtils.runSafeSync(LOG, new Runnable() {

                public void run() {

                    setFollowing(null);

                    actionManager.getEditorPool().clear();

                    session.removeListener(sharedProjectListener);
                    session.removeActivityProducer(EditorManager.this);
                    session.removeActivityConsumer(consumer);

                    session = null;

                    remoteEditorManager = null;
                    remoteWriteAccessManager.dispose();
                    remoteWriteAccessManager = null;
                    activeEditor = null;
                    locallyOpenEditors.clear();
                }
            });
        }


        @Override
        public void projectAdded(String projectID) {
            if (!isFollowing()) {
                return;
            }

            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                /*
                  * When Alice invites Bob to a session with a project and Alice
                  * has some Files of the shared project already open, Bob will
                  * not receive any Actions (Selection, Contribution etc.) for
                  * the open editors. When Alice closes and reopens this Files
                  * again everything is going back to normal. To prevent that
                  * from happening this method is needed.
                  */
                public void run() {

                    Set<SPath> remoteOpenEditors = getRemoteEditorManager().getRemoteOpenEditors(getFollowedUser());
                    RemoteEditorManager.RemoteEditor remoteSelectedEditor = getRemoteEditorManager().getRemoteActiveEditor(getFollowedUser());
                    Set<SPath> localOpenEditors = getLocallyOpenEditors();

                    // for every open file we act as if we just
                    // opened it
                    for (SPath remoteEditorPath : remoteOpenEditors) {
                        // Make sure that we open those editors twice
                        // (print a warning)
                        LOG.debug("Remote editor open " + remoteEditorPath);
                        if (!localOpenEditors.contains(remoteEditorPath)) {
                            getActionManager().openEditor(remoteEditorPath);
                        }
                    }


                    if (remoteSelectedEditor != null) {
                        //activate editor
                        SPath remotePath = remoteSelectedEditor.getPath();
                        if (remoteSelectedEditor.getSelection() != null) {
                            int position = remoteSelectedEditor.getSelection().getOffset();
                            int length = remoteSelectedEditor.getSelection().getLength();
                            ColorModel colorModel = ColorManager.getColorModel(getFollowedUser().getColorID());
                            getActionManager().selectText(remotePath, position, length, colorModel);
                        }

                        if (remoteSelectedEditor.getViewport() != null) {
                            int startLine = remoteSelectedEditor.getViewport().getStartLine();
                            int endLine = remoteSelectedEditor.getViewport().getStartLine() + remoteSelectedEditor.getViewport().getNumberOfLines();
                            getActionManager().setViewPort(remotePath, startLine, endLine);
                        }

                    }

                }
            });
        }
    };

    private ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {

        @Override
        public void activeEditorChanged(User user, SPath path) {

            // We only need to react to remote users changing editor
            if (user.isLocal()) {
                return;
            }

            // Clear all viewport annotations of this user.
            for (SPath editorPath : getLocallyOpenEditors()) {
                getActionManager().clearSelection(editorPath);
            }
        }
    };

    private Blockable stopManagerListener = new Blockable() {

        @Override
        public void unblock() {
            ThreadUtils.runSafeSync(LOG, new Runnable() {


                public void run() {
                    actionManager.lockAllEditors(false);
                }
            });
        }


        @Override
        public void block() {
            ThreadUtils.runSafeSync(LOG, new Runnable() {


                public void run() {
                    actionManager.lockAllEditors(true);
                }
            });
        }
    };

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {


        @Override
        public void permissionChanged(final User user) {

            hasWriteAccess = session.hasWriteAccess();

            // Lock / unlock editors
            if (user.isLocal()) {
                actionManager.lockAllEditors(hasWriteAccess);
            }

            refreshAnnotations();
        }


        @Override
        public void userFinishedProjectNegotiation(User user) {

            // Send awareness-informations
            User localUser = session.getLocalUser();
            for (SPath path : getLocallyOpenEditors()) {
                fireActivity(new EditorActivity(localUser, EditorActivity.Type.ACTIVATED, path));
            }

            fireActivity(new EditorActivity(localUser, EditorActivity.Type.ACTIVATED,
                    activeEditor));

            if (activeEditor == null) {
                return;
            }
            if (localViewport != null) {
                fireActivity(new ViewportActivity(localUser,
                        localViewport.getStartLine(),
                        localViewport.getNumberOfLines(), activeEditor));
            } else {
                LOG.warn("No viewport for locallyActivateEditor: "
                        + activeEditor);
            }

            if (localSelection != null) {
                int offset = localSelection.getNewRange().getStartOffset();
                int length = localSelection.getNewRange().getEndOffset() - localSelection.getNewRange().getStartOffset();

                fireActivity(new TextSelectionActivity(localUser, offset,
                        length, activeEditor));
            } else {
                LOG.warn("No selection for locallyActivateEditor: "
                        + activeEditor);
            }
        }


        @Override
        public void userLeft(final User user) {

            // If the user left which I am following, then stop following...
            if (user.equals(followedUser)) {
                setFollowing(null);
            }


            remoteEditorManager.removeUser(user);
        }
    };

    public EditorManager(ISarosSessionManager sessionManager, IPreferenceStore preferenceStore) {

        remoteEditorManager = new RemoteEditorManager(session);
        sessionManager.addSarosSessionListener(this.sessionListener);
        this.preferenceStore = preferenceStore;

        addSharedEditorListener(sharedEditorListener);


    }

    protected void execEditorActivity(EditorActivity editorActivity) {

        SPath path = editorActivity.getPath();
        if (path == null) {
            return;
        }

        LOG.debug(path + " editor activity received " + editorActivity);

        final User user = editorActivity.getSource();

        switch (editorActivity.getType()) {
            case ACTIVATED:
                if (isFollowing(user)) {
                    actionManager.openEditor(path);
                }
                editorListenerDispatch.activeEditorChanged(user, path);
                break;

            case CLOSED:
                if (isFollowing(user)) {
                    actionManager.closeEditor(path);
                }
                editorListenerDispatch.editorRemoved(user, path);
                break;
            case SAVED:
                actionManager.saveEditor(path);
                editorListenerDispatch.userWithWriteAccessEditorSaved(path, true);
                break;
            default:
                LOG.warn("Unexpected type: " + editorActivity.getType());
        }


    }

    protected void execTextEdit(TextEditActivity editorActivity) {

        SPath path = editorActivity.getPath();

        LOG.debug(path + " text edit activity received " + editorActivity);

        User user = editorActivity.getSource();
        ColorModel colorModel = ColorManager.getColorModel(user.getColorID());

        actionManager.editText(path, editorActivity.toOperation(), colorModel.getEditColor());

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(user, path, editorActivity.getText(),
                editorActivity.getReplacedText(), editorActivity.getOffset());


    }

    protected void execTextSelection(TextSelectionActivity selection) {

        SPath path = selection.getPath();

        LOG.debug(path + " text selection activity received " + selection);

        if (path == null) {
            return;
        }


        User user = selection.getSource();
        ColorModel colorModel = ColorManager.getColorModel(user.getColorID());

        if (isFollowing(user)) {
            actionManager.selectText(path, selection.getOffset(), selection.getLength(), colorModel);
        }

        /*
         * inform all registered ISharedEditorListeners about a text selection
         * made
         */
        editorListenerDispatch.textSelectionMade(selection);


    }

    protected void execViewport(ViewportActivity viewport) {

        SPath path = viewport.getPath();
        LOG.debug(path + " viewport activity received " + viewport);
        if (path == null) {
            return;
        }

        User user = viewport.getSource();
        if (isFollowing(user)) {
            actionManager.setViewPort(path, viewport.getStartLine(), viewport.getStartLine() + viewport.getNumberOfLines());
        }

        /*
         * inform all registered ISharedEditorListeners about a change in
         * viewport
         */
        editorListenerDispatch.viewportChanged(viewport);
    }


    public void exec(IActivity activity) {

        User sender = activity.getSource();
        if (!sender.isInSession()) {
            LOG.warn("skipping execution of activity " + activity
                    + " for user " + sender + " who is not in the current session");
            return;
        }

        // First let the remote managers update itself based on the
        // Activity
        remoteEditorManager.exec(activity);
        // remoteWriteAccessManager.exec(activity); //todo


        activity.dispatch(activityReceiver);
    }

    /**
     * Sets the local editor 'opened' and fires an {@link EditorActivity} of
     * type {@link de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type#ACTIVATED}.
     *
     * @param path the project-relative path to the resource that the editor is
     *             currently editing or <code>null</code> if the local user has
     *             no editor open.
     */
    public void generateEditorActivated(@Nullable SPath path) {

        this.activeEditor = path;

        if (path != null && session.isShared(path.getResource())) {
            this.locallyOpenEditors.add(path);
        }

        editorListenerDispatch.activeEditorChanged(session.getLocalUser(), path);
        fireActivity(new EditorActivity(session.getLocalUser(),
                EditorActivity.Type.ACTIVATED, path));

        //  generateSelection(path, selection);  //todo add this feature
        //  generateViewport(path, viewport);    //todo add this feature

    }

    public void generateEditorClosed(@Nullable SPath path) {
        // if closing the followed editor, leave follow mode
        if (getFollowedUser() != null) {
            RemoteEditorManager.RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                    getFollowedUser()).getActiveEditor();

            if (activeEditor != null && activeEditor.getPath().equals(path)) {
                // follower closed the followed editor (no other editor gets
                // activated)
                setFollowing(null);
                NotificationHandler.showNotification("Follow Mode stopped!", "You closed the followed editor.");
            }
        }


        fireActivity(new EditorActivity(session.getLocalUser(),
                EditorActivity.Type.CLOSED, path));
    }

    /**
     * Fires an update of the given {@link ITextSelection} for the given
     * {@link IEditorPart} so that all remote parties know that the user
     * selected some text in the given part.
     *
     * @param path         The IEditorPart for which to generate a TextSelectionActivity
     * @param newSelection The ITextSelection in the given part which represents the
     *                     currently selected text in editor.
     */
    public void generateSelection(SPath path, SelectionEvent newSelection) {

        if (path.equals(activeEditor)) {
            localSelection = newSelection;
        }

        int offset = newSelection.getNewRange().getStartOffset();
        int length = newSelection.getNewRange().getLength();

        fireActivity(new TextSelectionActivity(session.getLocalUser(),
                offset, length, path));
    }


    public void generateViewport(SPath path, LineRange viewport) {

        if (this.session == null) {
            LOG.warn("SharedEditorListener not correctly unregistered!");
            return;
        }


        if (path.equals(activeEditor)) {
            this.localViewport = viewport;
        }


        fireActivity(new ViewportActivity(session.getLocalUser(),
                viewport.getStartLine(), viewport.getNumberOfLines(), path));

        //  editorListenerDispatch.viewportGenerated(part, viewport, path);  //todo add this feature
    }

    /**
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
     */
    public boolean isFollowing() {
        return getFollowedUser() != null;
    }

    /**
     * Returns <code>true</code> if it is currently following user, otherwise <code>false</code>.
     */
    public boolean isFollowing(User user) {
        return getFollowedUser() != null && getFollowedUser().equals(user);
    }

    /**
     * Returns the followed {@link User} or <code>null</code> if currently no
     * user is followed.
     */
    public User getFollowedUser() {
        return followedUser;
    }

    /**
     * Sets the {@link User} to follow or <code>null</code> if no user should be
     * followed.
     */
    public void setFollowing(User newFollowedUser) {
        assert newFollowedUser == null
                || !newFollowedUser.equals(session.getLocalUser()) : "local user cannot follow himself!";

        User oldFollowedUser = this.followedUser;
        this.followedUser = newFollowedUser;

        if (oldFollowedUser != null && !oldFollowedUser.equals(newFollowedUser)) {
            editorListenerDispatch.followModeChanged(oldFollowedUser, false);
        }

        if (newFollowedUser != null) {
            editorListenerDispatch.followModeChanged(newFollowedUser, true);
            this.jumpToUser(newFollowedUser);
        }
    }

    public void jumpToUser(User jumpTo) {

        RemoteEditorManager.RemoteEditor activeEditor = remoteEditorManager.getEditorState(jumpTo)
                .getActiveEditor();

        // you can't follow yourself
        if (session.getLocalUser().equals(jumpTo)) {
            return;
        }

        if (activeEditor == null) {
            LOG.info(jumpTo.getJID() + " has no editor open");

            return;
        }

        Editor newEditor = this.actionManager.openEditor(activeEditor.getPath());

        if (newEditor == null) {
            return;
        }

        LineRange viewport = activeEditor.getViewport();

        if (viewport == null) {
            LOG.warn(jumpTo.getJID() + " has no viewport in editor: "
                    + activeEditor.getPath());
            return;
        }

        // selection can be null
        TextSelection selection = remoteEditorManager.getSelection(followedUser);
        if (selection != null) {
            this.actionManager.adjustViewport(newEditor, viewport, selection);
        }

        /*
         * inform all registered ISharedEditorListeners about this jump
         * performed
         */
        editorListenerDispatch.jumpedToUser(jumpTo);
    }


    public void setAllLocalOpenedEditorsLocked(boolean locked) {
        for (Editor editor : getActionManager().getEditorPool().getEditors()) {
            if (locked) {
                getActionManager().stopEditor(editor);
            } else {
                getActionManager().startEditor(editor);
            }
        }
    }

    //todo: needs implementation
    public void refreshAnnotations() {

    }


    public RemoteEditorManager getRemoteEditorManager() {
        return remoteEditorManager;
    }


    public boolean isActiveEditorShared() {
        if (activeEditor == null) {
            return false;
        }

        return isSharedEditor(activeEditor);
    }


    protected boolean isSharedEditor(SPath editorFilePath) {
        if (session == null) {
            return false;
        }

        if (!getActionManager().isOpenEditor(editorFilePath)) {
            return false;
        }

        return this.session.isShared(editorFilePath.getResource());
    }

    public void addSharedEditorListener(ISharedEditorListener listener) {
        this.editorListenerDispatch.add(listener);
    }

    public void removeSharedEditorListener(ISharedEditorListener listener) {
        this.editorListenerDispatch.remove(listener);
    }


    public Set<SPath> getLocallyOpenEditors() {
        return actionManager.getEditorPool().getFiles();
    }


    public Set<SPath> getRemoteOpenEditors() {
        return remoteEditorManager.getRemoteOpenEditors();
    }

    //todo: not sure how to do it intelliJ
    public void sendEditorActivitySaved(SPath path) {

    }


    /**
     * Generates a TextEditActivity and fires it.
     *
     * @param offset
     * @param oldText
     * @param newText
     * @param path
     */
    public synchronized void generateTextEdit(int offset, String oldText, String newText, SPath path) {

        if (session == null) {
            return;
        }

        TextEditActivity textEdit = new TextEditActivity(session.getLocalUser(), offset, oldText, newText, path);

        if (!hasWriteAccess || isLocked) {
           /*
             * TODO If we don't have {@link User.Permission#WRITE_ACCESS}, then
             * receiving this event might indicate that the user somehow
             * achieved to change his document. We should run a consistency
             * check.
             *
             * But watch out for changes because of a consistency check!
             */

            LOG.warn("local user caused text changes: " + textEdit
                    + " | write access : " + hasWriteAccess + ", session locked : "
                    + isLocked);
            return;
        }

        fireActivity(textEdit);

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(session.getLocalUser(),
                textEdit.getPath(), textEdit.getText(), textEdit.getReplacedText(), textEdit.getOffset());

    }


    public EditorActionManager getActionManager() {
        return actionManager;
    }

    public ISarosSession getSession() {
        return session;
    }

    public boolean hasSession() {
        return session != null;
    }
}