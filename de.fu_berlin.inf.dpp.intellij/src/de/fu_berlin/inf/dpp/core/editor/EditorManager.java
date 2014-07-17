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
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.EditorPool;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorManipulator;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorManager;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableDocumentListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableEditorFileListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableSelectionListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableViewPortListener;
import de.fu_berlin.inf.dpp.intellij.editor.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * The EditorManager is responsible for handling all editors in a DPP-session.
 *
 * This includes the functionality of listening for user inputs in an editor, listening for
 * remote inputs and locking the editors of the users with {@link Permission#READONLY_ACCESS}.
 *
 * This implementation uses the {@link de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler}
 * for actually accessing the editors. It translates the Activities for the EditorManager.
 */
public class EditorManager
        extends AbstractActivityProducer {

    private static final Logger LOG = Logger.getLogger(EditorManager.class);

    private SharedEditorListenerDispatch editorListenerDispatch = new SharedEditorListenerDispatch();

    private final LocalEditorHandler localEditorHandler;
    private final LocalEditorManipulator localEditorManipulator;

    private final EditorPool editorPool = new EditorPool();

    private RemoteEditorManager remoteEditorManager;

    private RemoteWriteAccessManager remoteWriteAccessManager;

    private ISarosSession session;

    private StoppableDocumentListener documentListener;
    private StoppableEditorFileListener fileListener;
    private StoppableSelectionListener selectionListener;
    private StoppableViewPortListener viewportListener;

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    private User followedUser = null;

    private boolean hasWriteAccess;

    private boolean isLocked;


    private Set<SPath> locallyOpenEditors = new HashSet<SPath>();

    private SelectionEvent localSelection;
    private LineRange localViewport;

    private SPath activeEditor;

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

            assert editorPool.getEditors().size() == 0 : "EditorPool was not correctly reset!";

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

                    editorPool.clear();

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
                            localEditorManipulator.openEditor(remoteEditorPath);
                        }
                    }


                    if (remoteSelectedEditor != null) {
                        //activate editor
                        SPath remotePath = remoteSelectedEditor.getPath();
                        if (remoteSelectedEditor.getSelection() != null) {
                            int position = remoteSelectedEditor.getSelection().getOffset();
                            int length = remoteSelectedEditor.getSelection().getLength();
                            ColorModel colorModel = ColorManager.getColorModel(getFollowedUser().getColorID());
                            localEditorManipulator.selectText(remotePath, position, length, colorModel);
                        }

                        if (remoteSelectedEditor.getViewport() != null) {
                            int startLine = remoteSelectedEditor.getViewport().getStartLine();
                            int endLine = remoteSelectedEditor.getViewport().getStartLine() + remoteSelectedEditor.getViewport().getNumberOfLines();
                            localEditorManipulator.setViewPort(remotePath, startLine, endLine);
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
                localEditorManipulator.clearSelection(editorPath);
            }
        }
    };

    private Blockable stopManagerListener = new Blockable() {

        @Override
        public void unblock() {
            ThreadUtils.runSafeSync(LOG, new Runnable() {
                public void run() {
                    unlockAllEditors();
                }
            });
        }


        @Override
        public void block() {
            ThreadUtils.runSafeSync(LOG, new Runnable() {


                public void run() {
                    lockAllEditors();
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
                if (hasWriteAccess) {
                    lockAllEditors();
                } else {
                    unlockAllEditors();
                }
            }

            refreshAnnotations();
        }


        @Override
        public void userFinishedProjectNegotiation(User user) {

            // Send awareness-information
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

    public EditorManager(ISarosSessionManager sessionManager, LocalEditorHandler localEditorHandler,
                         LocalEditorManipulator localEditorManipulator) {

        remoteEditorManager = new RemoteEditorManager(session);
        sessionManager.addSarosSessionListener(this.sessionListener);
        addSharedEditorListener(sharedEditorListener);
        this.localEditorHandler = localEditorHandler;
        this.localEditorManipulator = localEditorManipulator;

        this.documentListener = new StoppableDocumentListener(this);
        this.fileListener = new StoppableEditorFileListener(this);
        this.selectionListener = new StoppableSelectionListener(this);
        this.viewportListener = new StoppableViewPortListener(this);

        localEditorHandler.initialize(this);
        localEditorManipulator.initialize(this);
    }

    public Set<SPath> getLocallyOpenEditors() {
        return editorPool.getFiles();
    }

    public Set<SPath> getRemoteOpenEditors() {
        return remoteEditorManager.getRemoteOpenEditors();
    }

    public LocalEditorHandler getLocalEditorHandler() {
        return localEditorHandler;
    }

    public EditorPool getEditorPool() {
        return editorPool;
    }

    public ISarosSession getSession() {
        return session;
    }

    public boolean hasSession() {
        return session != null;
    }

    public StoppableDocumentListener getDocumentListener() {
        return documentListener;
    }

    public StoppableEditorFileListener getFileListener() {
        return fileListener;
    }

    private void execEditorActivity(EditorActivity editorActivity) {

        SPath path = editorActivity.getPath();
        if (path == null) {
            return;
        }

        LOG.debug(path + " editor activity received " + editorActivity);

        final User user = editorActivity.getSource();

        switch (editorActivity.getType()) {
            case ACTIVATED:
                if (isFollowing(user)) {
                    localEditorManipulator.openEditor(path);
                }
                editorListenerDispatch.activeEditorChanged(user, path);
                break;

            case CLOSED:
                if (isFollowing(user)) {
                    localEditorManipulator.closeEditor(path);
                }
                editorListenerDispatch.editorRemoved(user, path);
                break;
            case SAVED:
                localEditorHandler.saveFile(path);
                editorListenerDispatch.userWithWriteAccessEditorSaved(path, true);
                break;
            default:
                LOG.warn("Unexpected type: " + editorActivity.getType());
        }
    }

    private void execTextEdit(TextEditActivity editorActivity) {

        SPath path = editorActivity.getPath();

        LOG.debug(path + " text edit activity received " + editorActivity);

        User user = editorActivity.getSource();
        ColorModel colorModel = ColorManager.getColorModel(user.getColorID());

        localEditorManipulator.editText(path, editorActivity.toOperation(), colorModel.getEditColor());

        editorListenerDispatch.textEditRecieved(user, path, editorActivity.getText(),
                editorActivity.getReplacedText(), editorActivity.getOffset());
    }

    private void execTextSelection(TextSelectionActivity selection) {

        SPath path = selection.getPath();

        LOG.debug(path + " text selection activity received " + selection);

        if (path == null) {
            return;
        }

        User user = selection.getSource();
        ColorModel colorModel = ColorManager.getColorModel(user.getColorID());

        localEditorManipulator.selectText(path, selection.getOffset(), selection.getLength(), colorModel);

        editorListenerDispatch.textSelectionMade(selection);
    }

    private void execViewport(ViewportActivity viewport) {

        SPath path = viewport.getPath();
        LOG.debug(path + " viewport activity received " + viewport);
        if (path == null) {
            return;
        }

        User user = viewport.getSource();
        if (isFollowing(user)) {
            localEditorManipulator.setViewPort(path, viewport.getStartLine(), viewport.getStartLine() + viewport.getNumberOfLines());
        }

        editorListenerDispatch.viewportChanged(viewport);
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

        //  generateSelection(path, selection);  //FIXME: add this feature
        //  generateViewport(path, viewport);    //FIXME:s add this feature

    }

    /**
     * Fires an EditorActivity.Type.CLOSED event for the given path and leaves following, if closing the followed editor.
     *
     * @param path
     */
    public void generateEditorClosed(@Nullable SPath path) {
        // if closing the followed editor, leave follow mode
        if (getFollowedUser() != null) {
            RemoteEditorManager.RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                    getFollowedUser()).getActiveEditor();

            if (activeEditor != null && activeEditor.getPath().equals(path)) {
                // follower closed the followed editor (no other editor gets
                // activated)
                setFollowing(null);
                NotificationPanel.showNotification("Follow Mode stopped!", "You closed the followed editor.");
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

        //  editorListenerDispatch.viewportGenerated(part, viewport, path);  //FIXME: add this feature
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

        editorListenerDispatch.textEditRecieved(session.getLocalUser(),
                textEdit.getPath(), textEdit.getText(), textEdit.getReplacedText(), textEdit.getOffset());
    }

    /**
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
     */
    public boolean isFollowing() {
        return followedUser != null;
    }

    /**
     * Returns <code>true</code> if it is currently following user, otherwise <code>false</code>.
     */
    public boolean isFollowing(User user) {
        return followedUser != null && getFollowedUser().equals(user);
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

    /**
     * Locally opens the editor that the User jumpTo has currently open.
     *
     * @param jumpTo
     */
    public void jumpToUser(User jumpTo) {

        RemoteEditorManager.RemoteEditor remoteActiveEditor = remoteEditorManager.getEditorState(jumpTo)
                .getActiveEditor();

        // you can't follow yourself
        if (session.getLocalUser().equals(jumpTo)) {
            return;
        }

        if (remoteActiveEditor == null) {
            LOG.info(jumpTo.getJID() + " has no editor open");

            return;
        }

        Editor newEditor = localEditorManipulator.openEditor(remoteActiveEditor.getPath());

        if (newEditor == null) {
            return;
        }

        LineRange viewport = remoteActiveEditor.getViewport();

        if (viewport == null) {
            LOG.warn(jumpTo.getJID() + " has no viewport in editor: "
                    + remoteActiveEditor.getPath());
            return;
        }

        // selection can be null
        TextSelection selection = remoteEditorManager.getSelection(followedUser);
        if (selection != null) {
            localEditorManipulator.adjustViewport(newEditor, viewport, selection);
        }

        editorListenerDispatch.jumpedToUser(jumpTo);
    }

    //FIXME: needs implementation
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

        if (!localEditorHandler.isOpenEditor(editorFilePath)) {
            return false;
        }

        return this.session.isShared(editorFilePath.getResource());
    }

    public void addSharedEditorListener(ISharedEditorListener listener) {
        editorListenerDispatch.add(listener);
    }

    public void removeSharedEditorListener(ISharedEditorListener listener) {
        editorListenerDispatch.remove(listener);
    }

    public void enableDocumentListener() {
        documentListener.setEnabled(true);
    }

    public void disableDocumentListener() {
        documentListener.setEnabled(false);
    }

    /**
     * Enables the documentListener, the fileListener, the selectionListener and the viewportListener if the parameter
     * is <code>true</code>, else disables them.
     *
     * @param enable
     */
    public void setListenerEnabled(boolean enable) {

        if (documentListener != null) {
            documentListener.setEnabled(enable);
        }

        if (fileListener != null) {
            fileListener.setEnabled(enable);
        }

        if (selectionListener != null) {
            selectionListener.setEnabled(enable);
        }

        if (viewportListener != null) {
            viewportListener.setEnabled(enable);
        }
    }

    /**
     * Sets the editor's document writable and adds StoppableSelectionListener, StoppableViewPortListener and the
     * documentListener.
     *
     * @param editor
     */
    public void startEditor(Editor editor) {
        editor.getDocument().setReadOnly(false);
        editor.getSelectionModel().addSelectionListener(selectionListener);
        editor.getScrollingModel().addVisibleAreaListener(viewportListener);
        documentListener.startListening(editor.getDocument());
    }

    /**
     * Stops an editor by removing all listeners.
     *
     * @param editor
     */
    public void stopEditor(Editor editor) {
        editor.getSelectionModel().removeSelectionListener(selectionListener);
        editor.getScrollingModel().removeVisibleAreaListener(viewportListener);
        documentListener.stopListening();
    }

    /**
     * Unlocks all editors in the editorPool.
     */
    public void unlockAllEditors() {
        setListenerEnabled(true);
        editorPool.unlockAllDocuments();
    }

    /**
     * Locks all open editors, by setting them to read-only.
     */
    public void lockAllEditors() {
        setListenerEnabled(false);
        editorPool.lockAllDocuments();
    }

    /**
     * Unlocks all locally open editors by starting them.
     */
    public void unlockAllLocalOpenedEditors() {
        for (Editor editor : editorPool.getEditors()) {
            startEditor(editor);
        }
    }
}