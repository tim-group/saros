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

package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.core.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.core.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.core.editor.RemoteEditorManager;
import de.fu_berlin.inf.dpp.core.editor.RemoteWriteAccessManager;
import de.fu_berlin.inf.dpp.core.editor.SharedEditorListenerDispatch;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorManager;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
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
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * The EditorManager is responsible for handling all local editors in a DPP-session.
 * <p/>
 * This includes the functionality of listening for user inputs in an editor, listening for
 * remote inputs and locking the editors of the users with
 * {@link User.Permission#READONLY_ACCESS}.
 * <p/>
 * This implementation delegates edit activites received from remote to
 * {@link LocalEditorManipulator} and gets called by {@link LocalEditorHandler}
 * for activities from local editors.
 */
public class EditorManager extends AbstractActivityProducer {

    private static final Logger LOG = Logger.getLogger(EditorManager.class);

    private final Blockable stopManagerListener = new Blockable() {

        @Override
        public void unblock() {
            executeInUIThreadSynchronous(new Runnable() {
                public void run() {
                    unlockAllEditors();
                }
            });
        }

        @Override
        public void block() {
            executeInUIThreadSynchronous(new Runnable() {
                public void run() {
                    lockAllEditors();
                }
            });
        }
    };

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {

        @Override
        public void exec(IActivity activity) {
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
                editorListenerDispatch
                    .userWithWriteAccessEditorSaved(path, true);
                break;
            default:
                LOG.warn("Unexpected type: " + editorActivity.getType());
            }
        }

        private void execTextEdit(TextEditActivity editorActivity) {

            SPath path = editorActivity.getPath();

            LOG.debug(path + " text edit activity received " + editorActivity);

            User user = editorActivity.getSource();
            ColorModel colorModel = ColorManager
                .getColorModel(user.getColorID());

            localEditorManipulator
                .applyTextOperations(path, editorActivity.toOperation(),
                    colorModel.getEditColor());

            editorListenerDispatch
                .textEditRecieved(user, path, editorActivity.getText(),
                    editorActivity.getReplacedText(),
                    editorActivity.getOffset());
        }

        private void execTextSelection(TextSelectionActivity selection) {

            SPath path = selection.getPath();

            if (path == null) {
                return;
            }

            LOG.debug(
                "Text selection activity received: " + path + ", " + selection);

            User user = selection.getSource();
            ColorModel colorModel = ColorManager
                .getColorModel(user.getColorID());

            localEditorManipulator
                .selectText(path, selection.getOffset(), selection.getLength(),
                    colorModel);

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
                localEditorManipulator
                    .setViewPort(path, viewport.getStartLine(),
                        viewport.getStartLine() + viewport.getNumberOfLines());
            }

            editorListenerDispatch.viewportChanged(viewport);
        }
    };

    private final ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(final User user) {

            hasWriteAccess = session.hasWriteAccess();

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
                fireActivity(
                    new EditorActivity(localUser, EditorActivity.Type.ACTIVATED,
                        path)
                );
            }

            fireActivity(
                new EditorActivity(localUser, EditorActivity.Type.ACTIVATED,
                    activeEditor)
            );

            if (activeEditor == null) {
                return;
            }
            if (localViewport != null) {
                fireActivity(new ViewportActivity(localUser,
                    localViewport.getStartLine(),
                    localViewport.getNumberOfLines(), activeEditor));
            } else {
                LOG.warn(
                    "No viewport for locallyActivateEditor: " + activeEditor);
            }

            if (localSelection != null) {
                int offset = localSelection.getNewRange().getStartOffset();
                int length =
                    localSelection.getNewRange().getEndOffset() - localSelection
                        .getNewRange().getStartOffset();

                fireActivity(
                    new TextSelectionActivity(localUser, offset, length,
                        activeEditor)
                );
            } else {
                LOG.warn(
                    "No selection for locallyActivateEditor: " + activeEditor);
            }
        }

        @Override
        public void userLeft(final User user) {

            if (user.equals(followedUser)) {
                setFollowing(null);
            }

            remoteEditorManager.removeUser(user);
        }
    };
    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            startSession(newSarosSession);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            assert session == oldSarosSession;
            session.getStopManager()
                .removeBlockable(stopManagerListener); //todo

            executeInUIThreadSynchronous(new Runnable() {

                public void run() {
                    endSession();
                }
            });
        }

        @Override
        public void projectAdded(String projectID) {
            if (!isFollowing()) {
                return;
            }
            executeInUIThreadAsynchronous(new Runnable() {
                public void run() {
                    addProject();
                }
            });
        }

        private void startSession(ISarosSession newSarosSession) {
            assert editorPool.getEditors()
                .isEmpty() : "EditorPool was not correctly reset!";

            session = newSarosSession;
            session.getStopManager().addBlockable(stopManagerListener);

            hasWriteAccess = session.hasWriteAccess();
            session.addListener(sharedProjectListener);

            session.addActivityProducer(EditorManager.this);
            session.addActivityConsumer(consumer);

            remoteEditorManager = new RemoteEditorManager(session);
            remoteWriteAccessManager = new RemoteWriteAccessManager(session);

            //TODO: Test, whether this leads to problems because it is not called
            //from the UI thread.
            LocalFileSystem.getInstance().refresh(true);
        }

        private void endSession() {
            setFollowing(null);

            //This sets all editors, that were set to read only, writeable
            //again
            unlockAllEditors();
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

        private void addProject() {
             /*
              * When Alice invites Bob to a session with a project and Alice
              * has some Files of the shared project already open, Bob will
              * not receive any Actions (Selection, Contribution etc.) for
              * the open editors. When Alice closes and reopens this Files
              * again everything is going back to normal. To prevent that
              * from happening this method is needed.
              */
            Set<SPath> remoteOpenEditors = getRemoteEditorManager()
                .getRemoteOpenEditors(followedUser);
            RemoteEditorManager.RemoteEditor remoteSelectedEditor = getRemoteEditorManager()
                .getRemoteActiveEditor(followedUser);
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
                    int position = remoteSelectedEditor.getSelection()
                        .getOffset();
                    int length = remoteSelectedEditor.getSelection()
                        .getLength();
                    ColorModel colorModel = ColorManager
                        .getColorModel(followedUser.getColorID());
                    localEditorManipulator
                        .selectText(remotePath, position, length, colorModel);
                }

                if (remoteSelectedEditor.getViewport() != null) {
                    int startLine = remoteSelectedEditor.getViewport()
                        .getStartLine();
                    int endLine =
                        remoteSelectedEditor.getViewport().getStartLine()
                            + remoteSelectedEditor.getViewport()
                            .getNumberOfLines();
                    localEditorManipulator
                        .setViewPort(remotePath, startLine, endLine);
                }

            }
        }

    };

    private final ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {

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

    private final LocalEditorHandler localEditorHandler;
    private final LocalEditorManipulator localEditorManipulator;

    private final EditorPool editorPool = new EditorPool();

    private final SharedEditorListenerDispatch editorListenerDispatch = new SharedEditorListenerDispatch();
    private RemoteEditorManager remoteEditorManager;
    private RemoteWriteAccessManager remoteWriteAccessManager;
    private ISarosSession session;

    private final StoppableDocumentListener documentListener;
    private final StoppableEditorFileListener fileListener;
    private final StoppableSelectionListener selectionListener;
    private final StoppableViewPortListener viewportListener;

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    private User followedUser = null;
    private boolean hasWriteAccess;
    private boolean isLocked;
    private final Set<SPath> locallyOpenEditors = new HashSet<SPath>();
    private SelectionEvent localSelection;
    private LineRange localViewport;
    private SPath activeEditor;

    public EditorManager(ISarosSessionManager sessionManager,
        LocalEditorHandler localEditorHandler,
        LocalEditorManipulator localEditorManipulator) {

        remoteEditorManager = new RemoteEditorManager(session);
        sessionManager.addSarosSessionListener(sessionListener);
        addSharedEditorListener(sharedEditorListener);
        this.localEditorHandler = localEditorHandler;
        this.localEditorManipulator = localEditorManipulator;

        documentListener = new StoppableDocumentListener(this);
        fileListener = new StoppableEditorFileListener(this);
        selectionListener = new StoppableSelectionListener(this);
        viewportListener = new StoppableViewPortListener(this);

        localEditorHandler.initialize(this);
        localEditorManipulator.initialize(this);
    }

    public Set<SPath> getLocallyOpenEditors() {
        return editorPool.getFiles();
    }

    public Set<SPath> getRemoteOpenEditors() {
        return remoteEditorManager.getRemoteOpenEditors();
    }

    public void saveFile(SPath path) {
        localEditorHandler.saveFile(path);
    }

    public boolean isOpenedInEditor(SPath path) {
        return editorPool.getEditor(path) == null;
    }

    public void removeAllEditorsForPath(SPath path) {
        editorPool.removeAll(path);
    }

    public void replaceAllEditorsForPath(SPath oldPath, SPath newPath) {
        editorPool.replaceAll(oldPath, newPath);
    }

    /**
     * Returns the followed {@link User} or <code>null</code> if currently no
     * user is followed.
     */
    public User getFollowedUser() {
        return followedUser;
    }

    public RemoteEditorManager getRemoteEditorManager() {
        return remoteEditorManager;
    }

    public boolean isActiveEditorShared() {
        return activeEditor != null && isSharedEditor(activeEditor);
    }

    public void addSharedEditorListener(ISharedEditorListener listener) {
        editorListenerDispatch.add(listener);
    }

    public void removeSharedEditorListener(ISharedEditorListener listener) {
        editorListenerDispatch.remove(listener);
    }

    /**
     * This method is only for sending the initial content of a file
     * created with a template.
     *
     * @param spath
     * @param initialContent
     */
    public void sendTemplateContent(SPath spath, String initialContent) {
        //An Editor has to be activated, before it can be edited.
        generateEditorActivated(spath);
        generateTextEdit(0, initialContent, "", spath);
    }

    /**
     * Sets the {@link User} to follow or <code>null</code> if no user should be
     * followed. Calls {@link SharedEditorListenerDispatch#followModeChanged(User, boolean)}
     * to inform the other participants of the change.
     * <p/>
     * Jumps to the newly followed user.
     */
    public void setFollowing(User newFollowedUser) {
        assert newFollowedUser == null || !newFollowedUser.equals(
            session.getLocalUser()) : "local user cannot follow himself!";

        User oldFollowedUser = followedUser;
        followedUser = newFollowedUser;

        if (oldFollowedUser != null && !oldFollowedUser
            .equals(newFollowedUser)) {
            editorListenerDispatch.followModeChanged(oldFollowedUser, false);
        }

        if (newFollowedUser != null) {
            editorListenerDispatch.followModeChanged(newFollowedUser, true);
            jumpToUser(newFollowedUser);
        }
    }

    LocalEditorHandler getLocalEditorHandler() {
        return localEditorHandler;
    }

    EditorPool getEditorPool() {
        return editorPool;
    }

    ISarosSession getSession() {
        return session;
    }

    boolean hasSession() {
        return session != null;
    }

    StoppableDocumentListener getDocumentListener() {
        return documentListener;
    }

    StoppableEditorFileListener getFileListener() {
        return fileListener;
    }

    /**
     * Sets the local editor 'opened' and fires an {@link EditorActivity} of
     * type {@link EditorActivity.Type#ACTIVATED}.
     *
     * @param path the project-relative path to the resource that the editor is
     *             currently editing or <code>null</code> if the local user has
     *             no editor open.
     */
    void generateEditorActivated(SPath path) {

        activeEditor = path;

        if (path != null && session.isShared(path.getResource())) {
            locallyOpenEditors.add(path);
        }

        editorListenerDispatch
            .activeEditorChanged(session.getLocalUser(), path);
        fireActivity(new EditorActivity(session.getLocalUser(),
            EditorActivity.Type.ACTIVATED, path));

        //  generateSelection(path, selection);  //FIXME: add this feature
        //  generateViewport(path, viewport);    //FIXME:s add this feature

    }

    /**
     * Fires an EditorActivity.Type.CLOSED event for the given path and leaves following, if closing the followed editor.
     */
    void generateEditorClosed(SPath path) {
        // if closing the followed editor, leave follow mode
        if (followedUser != null) {
            RemoteEditorManager.RemoteEditor activeEditor = remoteEditorManager
                .getEditorState(followedUser).getActiveEditor();

            if (activeEditor != null && activeEditor.getPath().equals(path)) {
                // follower closed the followed editor (no other editor gets
                // activated)
                setFollowing(null);
                NotificationPanel.showNotification("Follow Mode stopped!",
                    "You closed the followed editor.");
            }
        }

        fireActivity(new EditorActivity(session.getLocalUser(),
            EditorActivity.Type.CLOSED, path));
    }

    /**
     * Generates a  {@link TextSelectionActivity}
     * and fires it.
     */
    void generateSelection(SPath path, SelectionEvent newSelection) {

        if (path.equals(activeEditor)) {
            localSelection = newSelection;
        }

        int offset = newSelection.getNewRange().getStartOffset();
        int length = newSelection.getNewRange().getLength();

        fireActivity(
            new TextSelectionActivity(session.getLocalUser(), offset, length,
                path)
        );
    }

    /**
     * Generates a  {@link ViewportActivity}
     * and fires it.
     */
    void generateViewport(SPath path, LineRange viewport) {

        if (session == null) {
            LOG.warn("SharedEditorListener not correctly unregistered!");
            return;
        }

        if (path.equals(activeEditor)) {
            localViewport = viewport;
        }

        fireActivity(new ViewportActivity(session.getLocalUser(),
            viewport.getStartLine(), viewport.getNumberOfLines(), path));

        //  editorListenerDispatch.viewportGenerated(part, viewport, path);  //FIXME: add this feature
    }

    /**
     * Generates a TextEditActivity and fires it.
     */
    void generateTextEdit(int offset, String newText, String replacedText,
        SPath path) {

        if (session == null) {
            return;
        }

        TextEditActivity textEdit = new TextEditActivity(session.getLocalUser(),
            offset, newText, replacedText, path);

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

        editorListenerDispatch
            .textEditRecieved(session.getLocalUser(), textEdit.getPath(),
                textEdit.getText(), textEdit.getReplacedText(),
                textEdit.getOffset());
    }

    /**
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
     */
    boolean isFollowing() {
        return followedUser != null;
    }

    /**
     * Returns <code>true</code> if it is currently following user, otherwise <code>false</code>.
     */
    boolean isFollowing(User user) {
        return followedUser != null && followedUser.equals(user);
    }

    /**
     * Locally opens the editor that the User jumpTo has currently open, adjusts
     * the viewport and calls {@link SharedEditorListenerDispatch#jumpedToUser(User)}
     * to inform the session participants of the jump.
     */
    void jumpToUser(User jumpTo) {

        RemoteEditorManager.RemoteEditor remoteActiveEditor = remoteEditorManager
            .getEditorState(jumpTo).getActiveEditor();

        // you can't follow yourself
        if (session.getLocalUser().equals(jumpTo)) {
            return;
        }

        if (remoteActiveEditor == null) {
            LOG.info(jumpTo.getJID() + " has no editor open");

            return;
        }

        Editor newEditor = localEditorManipulator
            .openEditor(remoteActiveEditor.getPath());

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
        TextSelection selection = remoteEditorManager
            .getSelection(followedUser);
        if (selection != null) {
            localEditorManipulator
                .adjustViewport(newEditor, viewport, selection);
        }

        editorListenerDispatch.jumpedToUser(jumpTo);
    }

    void refreshAnnotations() {
        //FIXME: needs implementation
    }

    boolean isSharedEditor(SPath editorFilePath) {
        if (session == null) {
            return false;
        }

        if (!localEditorHandler.isOpenEditor(editorFilePath)) {
            return false;
        }

        return session.isShared(editorFilePath.getResource());
    }

    void enableDocumentListener() {
        documentListener.setEnabled(true);
    }

    void disableDocumentListener() {
        documentListener.setEnabled(false);
    }

    /**
     * Enables the documentListener, the fileListener, the selectionListener and the viewportListener if the parameter
     * is <code>true</code>, else disables them.
     */
    void setListenerEnabled(boolean enable) {
        documentListener.setEnabled(enable);
        fileListener.setEnabled(enable);
        selectionListener.setEnabled(enable);
        viewportListener.setEnabled(enable);
    }

    /**
     * Sets the editor's document writable and adds StoppableSelectionListener, StoppableViewPortListener and the
     * documentListener.
     */
    void startEditor(Editor editor) {
        editor.getDocument().setReadOnly(isLocked || !hasWriteAccess);
        editor.getSelectionModel().addSelectionListener(selectionListener);
        editor.getScrollingModel().addVisibleAreaListener(viewportListener);
        documentListener.startListening(editor.getDocument());
    }

    /**
     * Stops an editor by removing all listeners.
     */
    void stopEditor(Editor editor) {
        editor.getDocument().setReadOnly(false);
        editor.getSelectionModel().removeSelectionListener(selectionListener);
        editor.getScrollingModel().removeVisibleAreaListener(viewportListener);
        documentListener.stopListening();
    }

    /**
     * Unlocks all editors in the editorPool.
     */
    void unlockAllEditors() {
        setListenerEnabled(true);
        editorPool.unlockAllDocuments();
    }

    /**
     * Locks all open editors, by setting them to read-only.
     */
    void lockAllEditors() {
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

    private void executeInUIThreadSynchronous(Runnable runnable) {
        ApplicationManager.getApplication()
            .invokeAndWait(runnable, ModalityState.NON_MODAL);
    }

    private void executeInUIThreadAsynchronous(Runnable runnable) {
        ApplicationManager.getApplication().invokeLater(runnable);
    }
}