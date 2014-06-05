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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.*;
import de.fu_berlin.inf.dpp.core.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorManager;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
import de.fu_berlin.inf.dpp.intellij.editor.mock.eclipse.*;
import de.fu_berlin.inf.dpp.intellij.editor.mock.internal.ContributionAnnotationManager;
import de.fu_berlin.inf.dpp.intellij.editor.mock.internal.LocationAnnotationManager;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.Annotation;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SarosView;
import de.fu_berlin.inf.dpp.intellij.util.Predicate;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

//todo: remove these links


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-16
 * Time: 18:57
 */

public class EditorManager
        // extends AbstractActivityProducerAndConsumer
        extends EditorManagerBridge
        implements IEditorManager {


    /**
     * @JTourBusStop 5, Some Basics:
     * <p/>
     * When you work on a project using Saros, you still use the
     * standard Eclipse Editor, however Saros adds a little extra
     * needed functionality to them.
     * <p/>
     * EditorManagerEcl is one of the most important classes in this
     * respect. Remember that every change done in an Editor needs
     * to be intercepted, translated into an Activity and sent to
     * all other participants. Furthermore every Activity from
     * other participants needs to be replayed in your local
     * editor when it is received.
     */

    protected static final Logger log = Logger.getLogger(EditorManager.class.getName());

    protected SharedEditorListenerDispatch editorListenerDispatch = new SharedEditorListenerDispatch();


    protected final EditorActionManager actionManager = new EditorActionManager(this);

    protected final IPreferenceStore preferenceStore;

    protected RemoteEditorManager remoteEditorManager;

    protected RemoteWriteAccessManager remoteWriteAccessManager;

    protected ISarosSession sarosSession;

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    protected User followedUser = null;

    protected boolean hasWriteAccess;

    protected boolean isLocked;


    protected final DirtyStateListener dirtyStateListener = new DirtyStateListener(this);


    protected Set<SPath> locallyOpenEditors = new HashSet<SPath>();

    protected SelectionEvent localSelection;
    protected ILineRange localViewport;

    protected SPath locallyActiveEditor;


    /**
     * all files that have connected document providers
     */
    protected final Set<IFile> connectedFiles = new HashSet<IFile>();

    //AnnotationModelHelper annotationModelHelper;
    LocationAnnotationManager locationAnnotationManager;
    ContributionAnnotationManager contributionAnnotationManager;

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

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            System.out.println("EditorManager.sessionStarted");
            sarosSession = newSarosSession;
            sarosSession.getStopManager().addBlockable(stopManagerListener);

//            assert editorPool.getAllEditors().size() == 0 : "EditorPool was not correctly reset!";
//
            hasWriteAccess = sarosSession.hasWriteAccess();
            sarosSession.addListener(sharedProjectListener);

            sarosSession.addActivityProducerAndConsumer(EditorManager.this);
            //annotationModelHelper = new AnnotationModelHelper();
            locationAnnotationManager = new LocationAnnotationManager(preferenceStore);
            contributionAnnotationManager = new ContributionAnnotationManager(newSarosSession, preferenceStore);
            remoteEditorManager = new RemoteEditorManager(sarosSession);
            remoteWriteAccessManager = new RemoteWriteAccessManager(sarosSession);

//            preferenceStore.addPropertyChangeListener(annotationPreferenceListener); //todo
//
//            SWTUtils.runSafeSWTSync(log, new Runnable()
//            {
//                @Override
//                public void run()
//                {
//
//                    editorAPI.addEditorPartListener(EditorManager.this);
//                }
//            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            System.out.println("EditorManager.sessionEnded");
            assert sarosSession == oldSarosSession;
            sarosSession.getStopManager().removeBlockable(stopManagerListener); //todo

            SWTUtils.runSafeSWTSync(log, new Runnable() {
                @Override
                public void run() {

                    setFollowing(null);

                    // editorAPI.removeEditorPartListener(EditorManager.this); //should be not needed

                    //  preferenceStore.removePropertyChangeListener(annotationPreferenceListener);  //todo

                    //todo: implement annotations
                    /*
                     * First need to remove the annotations and then clear the
                     * editorPool
                     */
                    actionManager.removeAnnotationsFromAllEditors(new Predicate<Annotation>() {
                        @Override
                        public boolean evaluate(Annotation annotation) {
                            return annotation instanceof SarosAnnotation;
                        }
                    });

                    actionManager.getEditorPool().clear(); //todo
                    //removeAllEditors(sarosSession); //make multi-session support

                    //  customAnnotationManager.uninstallAllPainters(true);  //todo

                    dirtyStateListener.unregisterAll();

                    sarosSession.removeListener(sharedProjectListener);
                    sarosSession.removeActivityProducerAndConsumer(EditorManager.this);

                    sarosSession = null;
                    //annotationModelHelper = null;
                    locationAnnotationManager = null;
                    contributionAnnotationManager.dispose();
                    contributionAnnotationManager = null;
                    remoteEditorManager = null;
                    remoteWriteAccessManager.dispose();
                    remoteWriteAccessManager = null;
                    locallyActiveEditor = null;
                    locallyOpenEditors.clear();
                }
            });
        }

        @Override
        public void projectAdded(String projectID) {
            //todo
            System.out.println("EditorManager.projectAdded //todo");
//            SWTUtils.runSafeSWTSync(log, new Runnable()
//            {
//
//                /*
//                 * When Alice invites Bob to a session with a project and Alice
//                 * has some Files of the shared project already open, Bob will
//                 * not receive any Actions (Selection, Contribution etc.) for
//                 * the open editors. When Alice closes and reopens this Files
//                 * again everything is going back to normal. To prevent that
//                 * from happening this method is needed.
//                 */
//                @Override
//                public void run()
//                {
//                    // Calling this method might cause openPart events
//                    Set<IEditorPart> allOpenEditorParts = EditorAPI
//                            .getOpenEditors();
//
//                    Set<IEditorPart> editorsOpenedByRestoring = editorPool
//                            .getAllEditors();
//
//                    // for every open file (editorPart) we act as if we just
//                    // opened it
//                    for (IEditorPart editorPart : allOpenEditorParts)
//                    {
//                        // Make sure that we open those editors twice
//                        // (print a warning)
//                        log.debug(editorPart.getTitle());
//                        if (!editorsOpenedByRestoring.contains(editorPart))
//                        {
//                            partOpened(editorPart);
//                        }
//                    }
//
//                    IEditorPart activeEditor = editorAPI.getActiveEditor();
//                    if (activeEditor != null)
//                    {
//                        locallyActiveEditor = editorAPI
//                                .getEditorPath(activeEditor);
//                        partActivated(activeEditor);
//                    }
//
//                }
//            });
        }
    };

    private ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {

        @Override
        public void activeEditorChanged(User user, SPath path) {
            System.out.println("EditorManager.activeEditorChanged>>>>>" + path);
        }
    };

    private Blockable stopManagerListener = new Blockable() {
        @Override
        public void unblock() {
            SWTUtils.runSafeSWTSync(log, new Runnable() {

                @Override
                public void run() {
                    actionManager.lockAllEditors(false);
                }
            });
        }

        @Override
        public void block() {
            SWTUtils.runSafeSWTSync(log, new Runnable() {

                @Override
                public void run() {
                    actionManager.lockAllEditors(true);
                }
            });
        }
    };

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(final User user) {

            // Make sure we have the up-to-date facts about ourself
            hasWriteAccess = sarosSession.hasWriteAccess();

            // Lock / unlock editors
            if (user.isLocal()) {
                actionManager.setWriteAccessEnabled(hasWriteAccess);
            }

            // TODO [PERF] 1 Make this lazy triggered on activating a part?
            refreshAnnotations();
        }

        @Override
        public void userFinishedProjectNegotiation(User user) {

            // TODO The user should be able to ask us for this state

            // Send awareness-informations
            User localUser = sarosSession.getLocalUser();
            for (SPath path : getLocallyOpenEditors()) {
                fireActivity(new EditorActivity(localUser, EditorActivity.Type.ACTIVATED, path));
            }

            fireActivity(new EditorActivity(localUser, EditorActivity.Type.ACTIVATED,
                    locallyActiveEditor));

            if (locallyActiveEditor == null) {
                return;
            }
            if (localViewport != null) {
                fireActivity(new ViewportActivity(localUser,
                        localViewport.getStartLine(),
                        localViewport.getNumberOfLines(), locallyActiveEditor));
            } else {
                log.warn("No viewport for locallyActivateEditor: "
                        + locallyActiveEditor);
            }

            if (localSelection != null) {
                int offset = localSelection.getNewRange().getStartOffset();
                int length = localSelection.getNewRange().getEndOffset() - localSelection.getNewRange().getStartOffset();

                fireActivity(new TextSelectionActivity(localUser, offset,
                        length, locallyActiveEditor));
            } else {
                log.warn("No selection for locallyActivateEditor: "
                        + locallyActiveEditor);
            }
        }

        @Override
        public void userLeft(final User user) {

            // If the user left which I am following, then stop following...
            if (user.equals(followedUser)) {
                setFollowing(null);
            }

            actionManager.removeAnnotationsFromAllEditors(new Predicate<Annotation>() {
                @Override
                public boolean evaluate(Annotation annotation) {
                    return annotation instanceof SarosAnnotation
                            && ((SarosAnnotation) annotation).getSource().equals(
                            user);
                }
            });
            remoteEditorManager.removeUser(user);
        }
    };

    /**
     * @Inject
     */
    public EditorManager(ISarosSessionManager sessionManager, IPreferenceStore preferenceStore) {

        System.out.println("EditorManager.EditorManager");

        remoteEditorManager = new RemoteEditorManager(sarosSession);
        //   registerCustomAnnotations();
        sessionManager.addSarosSessionListener(this.sessionListener);
        this.preferenceStore = preferenceStore;

        addSharedEditorListener(sharedEditorListener);


    }

    protected void execEditorActivity(EditorActivity editorActivity) {

        SPath path = editorActivity.getPath();
        if (path == null) {
            return;
        }

        final User user = editorActivity.getSource();

        System.out.println(">>>EditorManager.execEditorActivity " + editorActivity);


        switch (editorActivity.getType()) {
            case ACTIVATED:
                if (isFollowing(user)) {
                    actionManager.openFile(path);
                }
                editorListenerDispatch.activeEditorChanged(user, path);
                break;

            case CLOSED:
                if (isFollowing(user)) {
                    actionManager.closeFile(path);
                }
                editorListenerDispatch.editorRemoved(user, path);
                break;
            case SAVED:
                actionManager.saveFile(path);
                editorListenerDispatch.userWithWriteAccessEditorSaved(path, true);
                break;
            default:
                log.warn("Unexpected type: " + editorActivity.getType());
        }


    }

    ColorManager colorManager = new ColorManager(); //todo

    protected void execTextEdit(TextEditActivity editorActivity) {
        System.out.println(">>>EditorManager.execTextEdit " + editorActivity);

        SPath path = editorActivity.getPath();
        User user = editorActivity.getSource();
        ColorModel colorModel = colorManager.getColorModel(user.getColorID());

        actionManager.editText(path, editorActivity.toOperation(), colorModel.getEditColor());

        if (editorActivity.getReplacedText().contains("q"))
            return;


        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(user, path, editorActivity.getText(),
                editorActivity.getReplacedText(), editorActivity.getOffset());


    }

    protected void execTextSelection(TextSelectionActivity selection) {
        System.out.println(">>>EditorManager.execTextSelection " + selection);
//        SPath file = selection.getPath();
//        actionManager.selectText(file, selection.getOffset(), selection.getLength());
//


        SPath path = selection.getPath();
        if (path == null) {
            log.error("Received text selection but have no writable editor");
            return;
        }

        User user = selection.getSource();
        ColorModel colorModel = colorManager.getColorModel(user.getColorID());

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
        System.out.println(">>>EditorManager.execViewport " + viewport);
//        SPath file = editorActivity.getPath();
//        actionManager.setViewPort(file, editorActivity.getStartLine(), editorActivity.getStartLine() + editorActivity.getNumberOfLines());


        SPath path = viewport.getPath();
        if (path == null) {
            log.error("Received text selection but have no writable editor");
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


    @Override
    public void exec(IActivity activity) {

        User sender = activity.getSource();
        if (!sender.isInSarosSession()) {
            log.warn("skipping execution of activity " + activity
                    + " for user " + sender + " who is not in the current session");
            return;
        }

        // First let the remote managers update itself based on the
        // Activity
        remoteEditorManager.exec(activity);
//        remoteWriteAccessManager.exec(activity);


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

        this.locallyActiveEditor = path;

        if (path != null && sarosSession.isShared(path.getResource())) {
            this.locallyOpenEditors.add(path);
        }

        editorListenerDispatch.activeEditorChanged(sarosSession.getLocalUser(), path);
        fireActivity(new EditorActivity(sarosSession.getLocalUser(),
                EditorActivity.Type.ACTIVATED, path));

        //  generateSelection(path, selection);
        //  generateViewport(path, viewport);

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
                SarosView.showNotification("Follow Mode stopped!", "You closed the followed editor.");
            }
        }


        fireActivity(new EditorActivity(sarosSession.getLocalUser(),
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

        if (path.equals(locallyActiveEditor)) {
            localSelection = newSelection;
        }

        int offset = newSelection.getNewRange().getStartOffset();
        int length = newSelection.getNewRange().getLength();

        fireActivity(new TextSelectionActivity(sarosSession.getLocalUser(),
                offset, length, path));
    }


    public void generateViewport(SPath path, ILineRange viewport) {

        System.out.println("EditorManager.generateViewport " + viewport);

        if (this.sarosSession == null) {
            log.warn("SharedEditorListener not correctly unregistered!");
            return;
        }


        if (path.equals(locallyActiveEditor)) {
            this.localViewport = viewport;
        }


        fireActivity(new ViewportActivity(sarosSession.getLocalUser(),
                viewport.getStartLine(), viewport.getNumberOfLines(), path));

        //  editorListenerDispatch.viewportGenerated(part, viewport, path);
    }

    /**
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
     */
    public boolean isFollowing() {
        return getFollowedUser() != null;
    }

    /**
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
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
                || !newFollowedUser.equals(sarosSession.getLocalUser()) : "local user cannot follow himself!";

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
        if (sarosSession.getLocalUser().equals(jumpTo)) {
            return;
        }

        if (activeEditor == null) {
            log.info(jumpTo.getJID() + " has no editor open");

            // changed waldmann, 22.01.2012: this balloon Notification became
            // annoying as the awareness information, which file is opened is
            // now shown in the session view all the time (unless the user has
            // collapsed the tree element)

            // no active editor on target subject
            // SarosView.showNotification("Following " +
            // jumpTo.getJID().getBase()
            // + "!", jumpTo.getJID().getName()
            // + " has no shared file opened yet.");
            return;
        }

        Editor newEditor = this.actionManager.openFile(activeEditor.getPath());

        if (newEditor == null) {
            return;
        }

        ILineRange viewport = activeEditor.getViewport();

        if (viewport == null) {
            log.warn(jumpTo.getJID() + " has no viewport in editor: "
                    + activeEditor.getPath());
            return;
        }

        // selection can be null
        ITextSelection selection = remoteEditorManager.getSelection(followedUser);

        this.actionManager.adjustViewport(newEditor, viewport, selection);




        /*
         * inform all registered ISharedEditorListeners about this jump
         * performed
         */
        editorListenerDispatch.jumpedToUser(jumpTo);
    }


    @Override
    public void saveText(SPath path) {
        System.out.println("EditorManager.saveText");
    }

    @Override
    public Set<SPath> getOpenEditorsOfAllParticipants() {
        System.out.println("EditorManager.getOpenEditorsOfAllParticipants //todo");
        return new HashSet<SPath>(); //todo
    }

    @Override
    public void setAllLocalOpenedEditorsLocked(boolean locked) {
        System.out.println("EditorManager.setAllLocalOpenedEditorsLocked");
    }

    @Override
    public void colorChanged() {
        System.out.println("EditorManager.colorChanged");
    }

    @Override
    public void refreshAnnotations() {
        System.out.println("EditorManager.refreshAnnotations");
    }


    @Override
    public RemoteEditorManager getRemoteEditorManager() {
        return remoteEditorManager;
    }

    @Override
    public boolean isActiveEditorShared() {
        return false;
    }


    @Override
    public void addSharedEditorListener(ISharedEditorListener listener) {
        this.editorListenerDispatch.add(listener);
    }

    @Override
    public void saveLazy(SPath path) throws FileNotFoundException {
        getActionManager().saveFile(path);
    }

    @Override
    public void removeSharedEditorListener(ISharedEditorListener listener) {
        System.out.println("EditorManager.removeSharedEditorListener");
    }

    @Override
    public Set<SPath> getLocallyOpenEditors() {
        System.out.println("EditorManager.getLocallyOpenEditors");
        return actionManager.getEditorPool().getFiles();
    }

    @Override
    public Set<SPath> getRemoteOpenEditors() {
        //todo
        System.out.println("EditorManager.getRemoteOpenEditors //todo");
        return remoteEditorManager.getRemoteOpenEditors();
    }


    public void sendEditorActivitySaved(SPath path) {
        System.out.println("EditorManager.sendEditorActivitySaved ");
    }

    public boolean isConnected(IFile file) {
        return true;
    }


    /**
     * @param offset
     * @param oldText
     * @param newText
     * @param path
     */
    public void generateTextEdit(int offset, String oldText, String newText, SPath path) {

        if(sarosSession==null)
            return;

        TextEditActivity textEdit = new TextEditActivity(sarosSession.getLocalUser(), offset, oldText, newText, path);

        //        if (!hasWriteAccess || isLocked)
//        {
//            /**
//             * TODO If we don't have {@link User.Permission#WRITE_ACCESS}, then
//             * receiving this event might indicate that the user somehow
//             * achieved to change his document. We should run a consistency
//             * check.
//             *
//             * But watch out for changes because of a consistency check!
//             */
//            log.warn("local user caused text changes: " + textEdit
//                    + " | write access : " + hasWriteAccess + ", session locked : "
//                    + isLocked);
//            return;
//        }

        fireActivity(textEdit);

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(sarosSession.getLocalUser(),
                textEdit.getPath(), textEdit.getText(), textEdit.getReplacedText(), textEdit.getOffset());

//        /*
//         * TODO Investigate if this is really needed here
//         */
//        {
//            IEditorInput input = changedEditor.getEditorInput();
//            IDocumentProvider provider = getDocumentProvider(input);
//            IAnnotationModel treeModel = provider.getAnnotationModel(input);
//            contributionAnnotationManager.splitAnnotation(treeModel, offset);
//        }
    }


    public EditorActionManager getActionManager() {
        return actionManager;
    }


}