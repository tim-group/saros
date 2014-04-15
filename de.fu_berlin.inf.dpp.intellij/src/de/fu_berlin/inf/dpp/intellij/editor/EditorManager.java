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

import com.sun.istack.internal.Nullable;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.*;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.core.exceptions.CoreException;
import de.fu_berlin.inf.dpp.core.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.core.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.*;
import de.fu_berlin.inf.dpp.intellij.editor.intl.events.IPropertyChangeListener;
import de.fu_berlin.inf.dpp.intellij.editor.intl.events.PropertyChangeEvent;
import de.fu_berlin.inf.dpp.intellij.editor.internal.*;
import de.fu_berlin.inf.dpp.intellij.editor.intl.exceptions.BadLocationException;
import de.fu_berlin.inf.dpp.intellij.editor.intl.text.*;
import de.fu_berlin.inf.dpp.intellij.editor.intl.ui.FileEditorInput;
import de.fu_berlin.inf.dpp.intellij.editor.intl.ui.IDocumentProvider;
import de.fu_berlin.inf.dpp.intellij.editor.intl.ui.IEditorInput;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SarosView;
import de.fu_berlin.inf.dpp.intellij.util.Predicate;
import de.fu_berlin.inf.dpp.session.*;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.util.HashSet;
import java.util.Set;

/**
 * The EditorManager is responsible for handling all editors in a DPP-session.
 * This includes the functionality of listening for user inputs in an editor,
 * locking the editors of the users with {@link Permission#READONLY_ACCESS} .
 *
 * The EditorManager contains the testable logic. All untestable logic should
 * only appear in a class of the {@link IEditorAPI} type. (CO: This is the
 * theory at least)
 **/

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.1
 * Time: 10.07
 */
@Component(module = "core")
public class EditorManager extends AbstractActivityProducerAndConsumer implements IEditorManager
{
    protected static final Logger log = Logger.getLogger(EditorManager.class);

    protected SharedEditorListenerDispatch editorListenerDispatch = new SharedEditorListenerDispatch();

    protected IEditorAPI editorAPI;

    protected final IPreferenceStore preferenceStore;

    protected RemoteEditorManager remoteEditorManager;

    protected RemoteWriteAccessManager remoteWriteAccessManager;

    /**
     * The user that is followed or <code>null</code> if no user is followed.
     */
    protected User followedUser = null;

    protected boolean hasWriteAccess;

    protected boolean isLocked;

    protected final EditorPool editorPool = new EditorPool(this);

    protected final DirtyStateListener dirtyStateListener = new DirtyStateListener(this);

    protected final StoppableDocumentListener documentListener = new StoppableDocumentListener(this);

    protected SPath locallyActiveEditor;

    protected Set<SPath> locallyOpenEditors = new HashSet<SPath>();

    protected ITextSelection localSelection;

    protected ILineRange localViewport;

    protected ISarosSession sarosSession;

    /**
     * all files that have connected document providers
     */
    protected final Set<IFile> connectedFiles = new HashSet<IFile>();

    AnnotationModelHelper annotationModelHelper;
    LocationAnnotationManager locationAnnotationManager;
    ContributionAnnotationManager contributionAnnotationManager;

    private final CustomAnnotationManager customAnnotationManager = new CustomAnnotationManager();

    private final IPropertyChangeListener annotationPreferenceListener = new IPropertyChangeListener()
    {
        @Override
        public void propertyChange(final PropertyChangeEvent event)
        {
            locationAnnotationManager.propertyChange(event,
                    editorPool.getAllEditors());
        }
    };

    private IActivityReceiver activityReceiver = new AbstractActivityReceiver()
    {
        @Override
        public void receive(EditorActivity editorActivity)
        {
            execEditorActivity(editorActivity);
        }

        @Override
        public void receive(TextEditActivity textEditActivity)
        {
            execTextEdit(textEditActivity);
        }

        @Override
        public void receive(TextSelectionActivity textSelectionActivity)
        {
            execTextSelection(textSelectionActivity);
        }

        @Override
        public void receive(ViewportActivity viewportActivity)
        {
            execViewport(viewportActivity);
        }
    };

    private Blockable stopManagerListener = new Blockable()
    {
        @Override
        public void unblock()
        {
            SWTUtils.runSafeSWTSync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    lockAllEditors(false);
                }
            });
        }

        @Override
        public void block()
        {
            SWTUtils.runSafeSWTSync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    lockAllEditors(true);
                }
            });
        }
    };

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener()
    {

        @Override
        public void permissionChanged(final User user)
        {

            // Make sure we have the up-to-date facts about ourself
            hasWriteAccess = sarosSession.hasWriteAccess();

            // Lock / unlock editors
            if (user.isLocal())
            {
                editorPool.setWriteAccessEnabled(hasWriteAccess);
            }

            // TODO [PERF] 1 Make this lazy triggered on activating a part?
            refreshAnnotations();
        }

        @Override
        public void userFinishedProjectNegotiation(User user)
        {

            // TODO The user should be able to ask us for this state

            // Send awareness-informations
            User localUser = sarosSession.getLocalUser();
            for (SPath path : getLocallyOpenEditors())
            {
                fireActivity(new EditorActivity(localUser, Type.ACTIVATED, path));
            }

            fireActivity(new EditorActivity(localUser, Type.ACTIVATED,
                    locallyActiveEditor));

            if (locallyActiveEditor == null)
            {
                return;
            }
            if (localViewport != null)
            {
                fireActivity(new ViewportActivity(localUser,
                        localViewport.getStartLine(),
                        localViewport.getNumberOfLines(), locallyActiveEditor));
            }
            else
            {
                log.warn("No viewport for locallyActivateEditor: "
                        + locallyActiveEditor);
            }

            if (localSelection != null)
            {
                int offset = localSelection.getOffset();
                int length = localSelection.getLength();

                fireActivity(new TextSelectionActivity(localUser, offset,
                        length, locallyActiveEditor));
            }
            else
            {
                log.warn("No selection for locallyActivateEditor: "
                        + locallyActiveEditor);
            }
        }

        @Override
        public void userLeft(final User user)
        {

            // If the user left which I am following, then stop following...
            if (user.equals(followedUser))
            {
                setFollowing(null);
            }

            removeAnnotationsFromAllEditors(new Predicate<Annotation>()
            {
                @Override
                public boolean evaluate(Annotation annotation)
                {
                    return annotation instanceof SarosAnnotation
                            && ((SarosAnnotation) annotation).getSource().equals(
                            user);
                }
            });
            remoteEditorManager.removeUser(user);
        }
    };

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener()
    {

        @Override
        public void sessionStarted(ISarosSession newSarosSession)
        {
            sarosSession = newSarosSession;
            sarosSession.getStopManager().addBlockable(stopManagerListener);

            assert editorPool.getAllEditors().size() == 0 : "EditorPool was not correctly reset!";

            hasWriteAccess = sarosSession.hasWriteAccess();
            sarosSession.addListener(sharedProjectListener);

            sarosSession.addActivityProducerAndConsumer(EditorManager.this);
            annotationModelHelper = new AnnotationModelHelper();
            locationAnnotationManager = new LocationAnnotationManager(preferenceStore);
            contributionAnnotationManager = new ContributionAnnotationManager(newSarosSession, preferenceStore);
            remoteEditorManager = new RemoteEditorManager(sarosSession);
            remoteWriteAccessManager = new RemoteWriteAccessManager(sarosSession);

            preferenceStore.addPropertyChangeListener(annotationPreferenceListener);

            SWTUtils.runSafeSWTSync(log, new Runnable()
            {
                @Override
                public void run()
                {

                    editorAPI.addEditorPartListener(EditorManager.this);
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession)
        {

            assert sarosSession == oldSarosSession;
            sarosSession.getStopManager().removeBlockable(stopManagerListener);

            SWTUtils.runSafeSWTSync(log, new Runnable()
            {
                @Override
                public void run()
                {

                    setFollowing(null);

                    editorAPI.removeEditorPartListener(EditorManager.this);

                    preferenceStore.removePropertyChangeListener(annotationPreferenceListener);

                    /*
                     * First need to remove the annotations and then clear the
                     * editorPool
                     */
                    removeAnnotationsFromAllEditors(new Predicate<Annotation>()
                    {
                        @Override
                        public boolean evaluate(Annotation annotation)
                        {
                            return annotation instanceof SarosAnnotation;
                        }
                    });

                    editorPool.removeAllEditors(sarosSession);

                    customAnnotationManager.uninstallAllPainters(true);

                    dirtyStateListener.unregisterAll();

                    sarosSession.removeListener(sharedProjectListener);
                    sarosSession.removeActivityProducerAndConsumer(EditorManager.this);

                    sarosSession = null;
                    annotationModelHelper = null;
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
        public void projectAdded(String projectID)
        {
            SWTUtils.runSafeSWTSync(log, new Runnable()
            {

                /*
                 * When Alice invites Bob to a session with a project and Alice
                 * has some Files of the shared project already open, Bob will
                 * not receive any Actions (Selection, Contribution etc.) for
                 * the open editors. When Alice closes and reopens this Files
                 * again everything is going back to normal. To prevent that
                 * from happening this method is needed.
                 */
                @Override
                public void run()
                {
                    // Calling this method might cause openPart events
                    Set<IEditorPart> allOpenEditorParts = EditorAPI.getOpenEditors();

                    Set<IEditorPart> editorsOpenedByRestoring = editorPool
                            .getAllEditors();

                    // for every open file (editorPart) we act as if we just
                    // opened it
                    for (IEditorPart editorPart : allOpenEditorParts)
                    {
                        // Make sure that we open those editors twice
                        // (print a warning)
                        log.debug(editorPart.getTitle());
                        if (!editorsOpenedByRestoring.contains(editorPart))
                        {
                            partOpened(editorPart);
                        }
                    }

                    IEditorPart activeEditor = editorAPI.getActiveEditor();
                    if (activeEditor != null)
                    {
                        locallyActiveEditor = editorAPI
                                .getEditorPath(activeEditor);
                        partActivated(activeEditor);
                    }

                }
            });
        }
    };

    private ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener()
    {

        @Override
        public void activeEditorChanged(User user, SPath path)
        {
            // #2707089 We must clear annotations from shared editors that are
            // not commonly viewed

            // We only need to react to remote users changing editor
            if (user.isLocal())
            {
                return;
            }

            // Clear all viewport annotations of this user. That's not a problem
            // because the order of the activities is:
            // (1) EditorActivity (triggered this method call),
            // (2) TextSelectionActivity,
            // (3) ViewportActivity.
            for (IEditorPart editor : editorPool.getAllEditors())
            {
                locationAnnotationManager.clearViewportForUser(user, editor);
            }
        }
    };

    @Inject
    protected FileReplacementInProgressObservable fileReplacementInProgressObservable;


    /**
     * @Inject
     */
    public EditorManager(ISarosSessionManager sessionManager,
            EditorAPI editorApi, IPreferenceStore preferenceStore)
    {

        log.trace("EditorManager initialized");

        editorAPI = editorApi;
        this.preferenceStore = preferenceStore;
        registerCustomAnnotations();
        sessionManager.addSarosSessionListener(this.sessionListener);
        addSharedEditorListener(sharedEditorListener);
    }

    protected void execEditorActivity(EditorActivity editorActivity)
    {
        User sender = editorActivity.getSource();
        SPath sPath = editorActivity.getPath();
        switch (editorActivity.getType())
        {
            case ACTIVATED:
                execActivated(sender, sPath);
                break;
            case CLOSED:
                execClosed(sender, sPath);
                break;
            case SAVED:
                saveText(sPath);
                break;
            default:
                log.warn("Unexpected type: " + editorActivity.getType());
        }
    }

    protected void execTextEdit(TextEditActivity textEdit)
    {

        log.trace("EditorManager.execTextEdit invoked");

        SPath path = textEdit.getPath();
        // IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();
        IFile file = path.getFile();

        if (!file.exists())
        {
            log.error("TextEditActivity refers to file which"
                    + " is not available locally: " + textEdit);
            // TODO A consistency check can be started here
            return;
        }

        User user = textEdit.getSource();

        /*
         * Disable documentListener temporarily to avoid being notified of the
         * change
         */
        documentListener.setEnabled(false);

        replaceText(path, textEdit.getOffset(), textEdit.getReplacedText(),
                textEdit.getText(), user);

        documentListener.setEnabled(true);
        /*
         * If the text edit ends in the visible region of a local editor, set
         * the cursor annotation.
         *
         * TODO Performance optimization in case of batch operation might make
         * sense. Problem: How to recognize batch operations?
         */
        for (IEditorPart editorPart : editorPool.getEditors(path))
        {
            ITextViewer viewer = EditorAPI.getViewer(editorPart);
            if (viewer == null)
            {
                // No text viewer for the editorPart found.
                continue;
            }
            int cursorOffset = textEdit.getOffset()
                    + textEdit.getText().length();

            if (viewer.getTopIndexStartOffset() <= cursorOffset
                    && cursorOffset <= viewer.getBottomIndexEndOffset())
            {

                TextSelection selection = new TextSelection(cursorOffset, 0);
                locationAnnotationManager.setSelection(editorPart, selection, user);

                if (user.equals(getFollowedUser()))
                {
                    adjustViewport(user, editorPart, selection);
                }
            }
        }

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(user, path, textEdit.getText(),
                textEdit.getReplacedText(), textEdit.getOffset());
    }

    protected void execTextSelection(TextSelectionActivity selection)
    {

        log.trace("EditorManager.execTextSelection invoked");

        SPath path = selection.getPath();

        if (path == null)
        {
            EditorManager.log
                    .error("Received text selection but have no writable editor");
            return;
        }

        TextSelection textSelection = new TextSelection(selection.getOffset(),
                selection.getLength());

        User user = selection.getSource();

        Set<IEditorPart> editors = EditorManager.this.editorPool
                .getEditors(path);
        for (IEditorPart editorPart : editors)
        {
            locationAnnotationManager.setSelection(editorPart, textSelection,
                    user);

            if (user.equals(getFollowedUser()))
            {
                adjustViewport(user, editorPart, textSelection);
            }
        }

        /*
         * inform all registered ISharedEditorListeners about a text selection
         * made
         */
        editorListenerDispatch.textSelectionMade(selection);
    }

    protected void execViewport(ViewportActivity viewport)
    {

        log.trace("EditorManager.execViewport invoked");

        User user = viewport.getSource();
        boolean following = user.equals(getFollowedUser());

        {
            /**
             * Check if source is an observed user with
             * {@link User.Permission#WRITE_ACCESS} and his cursor is outside
             * the viewport.
             */
            ITextSelection userWithWriteAccessSelection = remoteEditorManager
                    .getSelection(user);
            /**
             * user with {@link User.Permission#WRITE_ACCESS} selection can be
             * null if ViewportActivity came before the first
             * TextSelectActivity.
             */
            if (userWithWriteAccessSelection != null)
            {
                /**
                 * TODO MR Taking the last line of the last selection of the
                 * user with {@link User.Permission#WRITE_ACCESS} might be a bit
                 * inaccurate.
                 */
                int userWithWriteAccessCursor = userWithWriteAccessSelection.getEndLine();

                int top = viewport.getStartLine();
                int bottom = viewport.getStartLine()
                        + viewport.getNumberOfLines();

                following = following
                        && (userWithWriteAccessCursor < top || userWithWriteAccessCursor > bottom);
            }
        }

        Set<IEditorPart> editors = this.editorPool.getEditors(viewport
                .getPath());

        ILineRange lineRange = new LineRange(viewport.getStartLine(),
                viewport.getNumberOfLines());

        for (IEditorPart editorPart : editors)
        {
            if (following || user.hasWriteAccess())
            {
                locationAnnotationManager.setViewportForUser(user, editorPart,
                        lineRange);
            }

            if (following)
            {
                adjustViewport(user, editorPart, lineRange);
            }
        }
        /*
         * inform all registered ISharedEditorListeners about a change in
         * viewport
         */
        editorListenerDispatch.viewportChanged(viewport);
    }

    protected void execActivated(User user, SPath path)
    {

        log.trace("EditorManager.execActivated invoked");

        editorListenerDispatch.activeEditorChanged(user, path);

        /**
         * Path null means this user with {@link User.Permission#WRITE_ACCESS}
         * has no active editor any more
         */
        if (user.equals(getFollowedUser()) && path != null)
        {

            // open editor but don't change focus
            editorAPI.openEditor(path, false);

        }
        else if (user.equals(getFollowedUser()) && path == null)
        {
            /*
             * Changed waldmann 22.01.2012: Since the currently opened file and
             * the information if no shared files are opened is permanently
             * shown in the saros view, this is no longer necessary and has
             * proven to be quite annoying to users too. This should only be
             * activated again if a preference is added to enable users to
             * disable this type of notification
             */

            // SarosView
            // .showNotification(
            // "Follow mode paused!",
            // user.getHumanReadableName()
            // +
            // " selected an editor that is not shared. \nFollow mode stays active and follows as soon as possible.");
        }
    }

    protected void execClosed(User user, SPath path)
    {

        log.trace("EditorManager.execClosed invoked");

        editorListenerDispatch.editorRemoved(user, path);

        if (user.equals(getFollowedUser()))
        {
            for (IEditorPart part : editorPool.getEditors(path))
            {
                editorAPI.closeEditor(part);
            }
        }
    }

    protected void execColorChanged()
    {
        editorListenerDispatch.colorChanged();
    }


    /**
     * Add annotation types and drawing strategies using the following two
     * method calls
     * {@link CustomAnnotationManager#registerAnnotation(String, int)
     * registerAnnotation()} and
     * {@link CustomAnnotationManager#registerDrawingStrategy(String, org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy)
     * registerDrawingStrategy()} .
     */
    private void registerCustomAnnotations()
    {
        /*
         * Explanation of the "layer magic": The six SelectionAnnotationTypes (1
         * default + 5 users) are located on layers 8 to 13 (see plugin.xml,
         * extension point "markerAnnotationSpecification").
         */
        int defaultSelectionLayer = 8;

        /*
         * For every SelectionFillUpAnnotation of a different color there is an
         * own layer. There is one layer for the default color and one for each
         * of the different user colors. All SelectionFillUpAnnotations are
         * drawn in lower levels than the RemoteCursorAnnotation.
         *
         * TODO: The color determines on which layer a selection will be drawn.
         * So, in "competitive" situations, some selections will never be
         * visible because they are always drawn in a lower layer. (Can only
         * happen in sessions with more than two participants: Carl selected a
         * block, Bob selects a statement within, Alice might not be able to see
         * Bob's selection until Carl's changes his.)
         */
        SelectionFillUpStrategy strategy = new SelectionFillUpStrategy();
        for (int i = 0; i <= SarosAnnotation.SIZE; i++)
        {
            String type = SarosAnnotation.getNumberedType(
                    SelectionFillUpAnnotation.TYPE, i);
            customAnnotationManager.registerAnnotation(type,
                    defaultSelectionLayer + i);
            customAnnotationManager.registerDrawingStrategy(type, strategy);
        }

        /*
         * The RemoteCursorAnnotations are drawn in the layer above.
         */
        customAnnotationManager.registerAnnotation(RemoteCursorAnnotation.TYPE,
                defaultSelectionLayer + SarosAnnotation.SIZE + 1);
        customAnnotationManager.registerDrawingStrategy(
                RemoteCursorAnnotation.TYPE, new RemoteCursorStrategy());
    }


    public boolean isConnected(IFile file)
    {
        return connectedFiles.contains(file);
    }

    /**
     * Tries to add the given {@link IFile} to a set of locally opened files.
     * The file gets connected to its {@link de.fu_berlin.inf.dpp.intellij.editor.intl.ui.IDocumentProvider} (e.g.
     * CompilationUnitDocumentProvider for Java-Files) This Method also converts
     * the line delimiters of the document. Already connected files will not be
     * connected twice.
     */
    public void connect(IFile file)
    {

        if (!file.isAccessible())
        {
            log.error(".connect File " + file.getName()
                    + " could not be accessed");
            return;
        }

        log.trace(".connect(" + file.getProjectRelativePath().toOSString()
                + ") invoked");

        if (!isConnected(file))
        {
            FileEditorInput input = new FileEditorInput(file);
            IDocumentProvider documentProvider = getDocumentProvider(input);
            try
            {
                documentProvider.connect(input);
            }
            catch (CoreException e)
            {
                log.error("Error connecting to a document provider on file '"
                        + file.toString() + "':", e);
            }
            connectedFiles.add(file);
        }
    }

    public void disconnect(IFile file)
    {

        log.trace(".disconnect(" + file.getProjectRelativePath().toOSString()
                + ") invoked");

        if (!isConnected(file))
        {
            log.warn(".disconnect(): Trying to disconnect"
                    + " DocProvider which is not connected: "
                    + file.getFullPath().toOSString());
            return;
        }

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider documentProvider = getDocumentProvider(input);
        documentProvider.disconnect(input);

        connectedFiles.remove(file);
    }

    /**
     * Locks/unlocks all Editors for writing operations. Locked means local
     * keyboard inputs are not applied.
     *
     * @param lock if true then editors are locked, otherwise they are unlocked
     */
    protected void lockAllEditors(boolean lock)
    {
        if (lock)
        {
            log.debug("Lock all editors");
        }
        else
        {
            log.debug("Unlock all editors");
        }
        editorPool
                .setWriteAccessEnabled(!lock && sarosSession.hasWriteAccess());

        isLocked = lock;
    }

    /**
     * Sets the {@link User} to follow or <code>null</code> if no user should be
     * followed.
     */
    public void setFollowing(User newFollowedUser)
    {
        assert newFollowedUser == null
                || !newFollowedUser.equals(sarosSession.getLocalUser()) : "local user cannot follow himself!";

        User oldFollowedUser = this.followedUser;
        this.followedUser = newFollowedUser;

        if (oldFollowedUser != null && !oldFollowedUser.equals(newFollowedUser))
        {
            editorListenerDispatch.followModeChanged(oldFollowedUser, false);
        }

        if (newFollowedUser != null)
        {
            editorListenerDispatch.followModeChanged(newFollowedUser, true);
            this.jumpToUser(newFollowedUser);
        }
    }

    public void jumpToUser(User jumpTo)
    {

        RemoteEditorManager.RemoteEditor activeEditor = remoteEditorManager.getEditorState(jumpTo)
                .getActiveEditor();

        // you can't follow yourself
        if (sarosSession.getLocalUser().equals(jumpTo))
        {
            return;
        }

        if (activeEditor == null)
        {
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

        IEditorPart newEditor = this.editorAPI.openEditor(activeEditor
                .getPath());

        if (newEditor == null)
        {
            return;
        }

        ILineRange viewport = activeEditor.getViewport();

        if (viewport == null)
        {
            log.warn(jumpTo.getJID() + " has no viewport in editor: "
                    + activeEditor.getPath());
            return;
        }

        adjustViewport(jumpTo, newEditor, viewport);

        /*
         * inform all registered ISharedEditorListeners about this jump
         * performed
         */
        editorListenerDispatch.jumpedToUser(jumpTo);
    }

    /*
    * Adjusts the viewport in Follow Mode. This function should be called if
        * the followed user's selection changes.
        *
        * @param followedUser
    *            User who is followed
    * @param editorPart
    *            EditorPart of the open Editor
    * @param selection
    *            text selection of the followed user
    */
    private void adjustViewport(User followedUser, IEditorPart editorPart,
            ITextSelection selection)
    {
        if (selection == null)
        {
            return;
        }

        // range can be null
        ILineRange range = remoteEditorManager.getViewport(followedUser);
        adjustViewport(editorPart, range, selection);
    }

    /**
     * Adjusts the viewport in Follow Mode. This function should be called if
     * the followed user's viewport changes.
     *
     * @param followedUser User who is followed
     * @param editorPart   EditorPart of the open Editor
     * @param range        viewport of the followed user
     */
    private void adjustViewport(User followedUser, IEditorPart editorPart,
            ILineRange range)
    {
        if (range == null)
        {
            return;
        }

        // selection can be null
        ITextSelection selection = remoteEditorManager
                .getSelection(followedUser);
        adjustViewport(editorPart, range, selection);
    }

    /**
     * Adjusts viewport. Focus is set on the center of the range, but priority
     * is given to selected lines. Either range or selection can be null, but
     * not both.
     *
     * @param editorPart EditorPart of the open Editor
     * @param range      viewport of the followed user. Can be <code>null</code>.
     * @param selection  text selection of the followed user. Can be <code>null</code>.
     */
    private void adjustViewport(IEditorPart editorPart, ILineRange range,
            ITextSelection selection)
    {
        ITextViewer viewer = EditorAPI.getViewer(editorPart);
        if (viewer == null)
        {
            return;
        }

        IDocument document = viewer.getDocument();
        ILineRange localViewport = EditorAPI.getViewport(viewer);

        if (localViewport == null || document == null)
        {
            return;
        }

        int lines = document.getNumberOfLines();
        int rangeTop = 0;
        int rangeBottom = 0;
        int selectionTop = 0;
        int selectionBottom = 0;

        if (selection != null)
        {
            try
            {
                selectionTop = document.getLineOfOffset(selection.getOffset());
                selectionBottom = document.getLineOfOffset(selection
                        .getOffset() + selection.getLength());
            }
            catch (BadLocationException e)
            {
                // should never be reached
                log.error("Invalid line selection: offset: "
                        + selection.getOffset() + ", length: "
                        + selection.getLength());

                selection = null;
            }
        }

        if (range != null)
        {
            if (range.getStartLine() == -1)
            {
                range = null;
            }
            else
            {
                rangeTop = Math.min(lines - 1, range.getStartLine());
                rangeBottom = Math.min(lines - 1,
                        rangeTop + range.getNumberOfLines());
            }
        }

        if (range == null && selection == null)
        {
            return;
        }

        // top line of the new viewport
        int topPosition;
        int localLines = localViewport.getNumberOfLines();
        int remoteLines = rangeBottom - rangeTop;
        int sizeDiff = remoteLines - localLines;

        // initializations finished

        if (range == null || selection == null)
        {
            topPosition = (rangeTop + rangeBottom + selectionTop + selectionBottom) / 2;
            viewer.setTopIndex(topPosition);
            return;
        }

        /*
         * usually the viewport of the follower and the viewport of the followed
         * user will have the same center (this calculation). Exceptions may be
         * made below.
         */
        int center = (rangeTop + rangeBottom) / 2;
        topPosition = center - localLines / 2;

        if (sizeDiff <= 0)
        {
            // no further examination necessary when the local viewport is the
            // larger one
            viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
            return;
        }

        boolean selectionTopInvisible = (selectionTop < rangeTop + sizeDiff / 2);
        boolean selectionBottomInvisible = (selectionBottom > rangeBottom
                - sizeDiff / 2 - 1);

        if (rangeTop == 0
                && !(selectionTop <= rangeBottom && selectionTop > rangeBottom
                - sizeDiff))
        {
            // scrolled to the top and no selection at the bottom of range
            topPosition = 0;

        }
        else if (rangeBottom == lines - 1
                && !(selectionBottom >= rangeTop && selectionBottom < rangeTop
                + sizeDiff))
        {
            // scrolled to the bottom and no selection at the top of range
            topPosition = lines - localLines;

        }
        else if (selectionTopInvisible && selectionBottom >= rangeTop)
        {
            // making selection at top of range visible
            topPosition = Math.max(rangeTop, selectionTop);

        }
        else if (selectionBottomInvisible && selectionTop <= rangeBottom)
        {
            // making selection at bottom of range visible
            topPosition = Math.min(rangeBottom, selectionBottom) - localLines
                    + 1;
        }

        viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
    }


    /**
     * Called when the local user opened an editor part.
     */
    public void partOpened(IEditorPart editorPart)
    {

        log.trace(".partOpened invoked");

        if (!isSharedEditor(editorPart))
        {
            return;
        }

        /*
         * If the resource is not accessible it might have been deleted without
         * the editor having been closed (for instance outside of Eclipse).
         * Others might be confused about if they receive this editor from us.
         */
        IResource editorResource = editorAPI.getEditorResource(editorPart);
        if (!editorResource.isAccessible())
        {
            log.warn(".partOpened resource: "
                    + editorResource.getLocation().toOSString()
                    + " is not accesible");
            return;
        }
    }

    /**
     * Called when the local user activated a shared editor.
     * <p/>
     * This can be called twice for a single IEditorPart, because it is called
     * from partActivated and from partBroughtToTop.
     * <p/>
     * We do not filter duplicate events, because it would be bad to miss events
     * and is not too bad have duplicate one's. In particular we use IPath as an
     * identifier to the IEditorPart which might not work for multiple editors
     * based on the same file.
     */
    public void partActivated(IEditorPart editorPart)
    {

        log.trace(".partActivated invoked");

        // First check for last editor being closed (which is a null editorPart)
        if (editorPart == null)
        {
            generateEditorActivated(null);
            return;
        }

        // Is the new editor part supported by Saros (and inside the project)
        // and the Resource accessible (we don't want to report stale files)?
        IResource resource = editorAPI.getEditorResource(editorPart);

        if (!isSharedEditor(editorPart)
                || !sarosSession.isShared(ResourceAdapterFactory.create(resource))
                || !editorAPI.getEditorResource(editorPart).isAccessible())
        {
            generateEditorActivated(null);
            // follower switched to another unshared editor or closed followed
            // editor (not shared editor gets activated)
            if (isFollowing())
            {
                setFollowing(null);
                SarosView
                        .showNotification(
                                "Follow Mode stopped!",
                                "You switched to another editor that is not shared \nor closed the followed editor.");
            }
            return;
        }

        /*
         * If the opened editor is not the active editor of the user being
         * followed, then leave follow mode
         */
        if (isFollowing())
        {
            RemoteEditorManager.RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                    getFollowedUser()).getActiveEditor();

            if (activeEditor != null
                    && !activeEditor.getPath().equals(
                    editorAPI.getEditorPath(editorPart)))
            {
                setFollowing(null);
                // follower switched to another shared editor or closed followed
                // editor (shared editor gets activated)
                SarosView.showNotification("Follow Mode stopped!",
                        "You switched to another editor \nor closed the followed editor.");
            }
        }

        SPath editorPath = this.editorAPI.getEditorPath(editorPart);
        ILineRange viewport = this.editorAPI.getViewport(editorPart);
        ITextSelection selection = this.editorAPI.getSelection(editorPart);

        // Set (and thus send) in this order:
        generateEditorActivated(editorPath);
        generateSelection(editorPart, selection);
        if (viewport == null)
        {
            log.warn("Shared Editor does not have a Viewport: " + editorPart);
        }
        else
        {
            generateViewport(editorPart, viewport);
        }

        ITextViewer viewer = EditorAPI.getViewer(editorPart);

        if (viewer instanceof ISourceViewer)
        {
            customAnnotationManager.installPainter((ISourceViewer) viewer);
        }

    }


    /**
     * Removes all Annotation that fulfill given {@link Predicate} from all
     * editors.
     *
     * @param predicate The filter to use for cleaning.
     */
    protected void removeAnnotationsFromAllEditors(
            Predicate<Annotation> predicate)
    {

        for (IEditorPart editor : this.editorPool.getAllEditors())
        {
            annotationModelHelper
                    .removeAnnotationsFromEditor(editor, predicate);
        }
    }

    public void addSharedEditorListener(ISharedEditorListener editorListener)
    {
        this.editorListenerDispatch.add(editorListener);
    }

    public void removeSharedEditorListener(ISharedEditorListener editorListener)
    {
        this.editorListenerDispatch.remove(editorListener);
    }


    /**
     * This method is called from Eclipse (via the StoppableDocumentListener)
     * whenever the local user has changed some text in an editor.
     *
     * @param offset        The index into the given document where the text change
     *                      started.
     * @param text          The text that has been inserted (is "" if no text was inserted
     *                      but just characters were removed)
     * @param replaceLength The number of characters which have been replaced by this edit
     *                      (is 0 if no character has been removed)
     * @param document      The document which was changed.
     */
    public void textAboutToBeChanged(int offset, String text,
            int replaceLength, IDocument document)
    {

        if (fileReplacementInProgressObservable.isReplacementInProgress())
        {
            return;
        }

        if (sarosSession == null)
        {
            log.error("session has ended but text edits"
                    + " are received from local user");
            return;
        }

        IEditorPart changedEditor = null;

        // FIXME: This is potentially slow and definitely ugly
        // search editor which changed
        for (IEditorPart editor : editorPool.getAllEditors())
        {
            if (ObjectUtils.equals(getDocument(editor), document))
            {
                changedEditor = editor;
                break;
            }
        }

        if (changedEditor == null)
        {
            log.error("Could not find editor for changed document " + document);
            return;
        }

        SPath path = editorAPI.getEditorPath(changedEditor);
        if (path == null)
        {
            log.warn("Could not find path for editor "
                    + changedEditor.getTitle());
            return;
        }

        String replacedText;
        try
        {
            replacedText = document.get(offset, replaceLength);
        }
        catch (BadLocationException e)
        {
            log.error("Offset and/or replace invalid", e);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < replaceLength; i++)
            {
                sb.append("?");
            }
            replacedText = sb.toString();
        }

        TextEditActivity textEdit = new TextEditActivity(
                sarosSession.getLocalUser(), offset, text, replacedText, path);

        if (!hasWriteAccess || isLocked)
        {
            /**
             * TODO If we don't have {@link User.Permission#WRITE_ACCESS}, then
             * receiving this event might indicate that the user somehow
             * achieved to change his document. We should run a consistency
             * check.
             *
             * But watch out for changes because of a consistency check!
             */
            log.warn("local user caused text changes: " + textEdit
                    + " | write access : " + hasWriteAccess + ", session locked : "
                    + isLocked);
            return;
        }

        fireActivity(textEdit);

        // inform all registered ISharedEditorListeners about this text edit
        editorListenerDispatch.textEditRecieved(sarosSession.getLocalUser(),
                path, text, replacedText, offset);

        /*
         * TODO Investigate if this is really needed here
         */
        {
            IEditorInput input = changedEditor.getEditorInput();
            IDocumentProvider provider = getDocumentProvider(input);
            IAnnotationModel model = provider.getAnnotationModel(input);
            contributionAnnotationManager.splitAnnotation(model, offset);
        }
    }

    /**
     * Returns the resource paths of editors that the local user is currently
     * using.
     *
     * @return all paths (in project-relative format) of files that the local
     * user is currently editing by using an editor. Never returns
     * <code>null</code>. A empty set is returned if there are no
     * currently opened editors.
     */
    public Set<SPath> getLocallyOpenEditors()
    {
        return this.locallyOpenEditors;
    }

    /**
     * Returns the followed {@link User} or <code>null</code> if currently no
     * user is followed.
     */
    public User getFollowedUser()
    {
        return followedUser;
    }

    /*
    * This method is called when a remote text edit has been received over the
    * network to apply the change to the local files.
    *
            * @param path
    *            The path in which the change should be made.
    *
            *            TODO We would like to be able to allow changing editors which
    *            are not driven by files someday, but it is not possible yet.
    *
            * @param offset
    *            The position into the document of the given file, where the
    *            change started.
        *
        * @param replacedText
    *            The text which is to be replaced by this operation at the
    *            given offset (is "" if this operation is only inserting text)
    *
            * @param text
    *            The text which is to be inserted at the given offset instead
    *            of the replaced text (is "" if this operation is only deleting
        *            text)
    * @param source
    *            The User who caused this change.
    */
    protected void replaceText(SPath path, int offset, String replacedText,
            String text, User source)
    {

        // IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();
        IFile file = path.getFile();
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = getDocumentProvider(input);

        try
        {
            provider.connect(input);
        }
        catch (CoreException e)
        {
            log.error(
                    "Could not connect document provider for file: "
                            + file.toString(), e
            );
            // TODO Trigger a consistency recovery
            return;
        }

        try
        {
            IDocument doc = provider.getDocument(input);
            if (doc == null)
            {
                log.error("Could not connect document provider for file: "
                        + file.toString(), new StackTrace());
                // TODO Trigger a consistency recovery
                return;
            }

            // Check if the replaced text is really there.
            if (log.isDebugEnabled())
            {

                String is;
                try
                {
                    is = doc.get(offset, replacedText.length());
                    if (!is.equals(replacedText))
                    {
                        log.error("replaceText should be '"
                                + StringEscapeUtils.escapeJava(replacedText)
                                + "' is '" + StringEscapeUtils.escapeJava(is) + "'");
                    }
                }
                catch (BadLocationException e)
                {
                    // Ignore, because this is going to fail again just below
                }
            }

            // Try to replace
            try
            {
                doc.replace(offset, replacedText.length(), text);
            }
            catch (BadLocationException e)
            {
                log.error(String.format(
                        "Could not apply TextEdit at %d-%d of document "
                                + "with length %d.\nWas supposed to replace"
                                + " '%s' with '%s'.", offset,
                        offset + replacedText.length(), doc.getLength(),
                        replacedText, text
                ));
                return;
            }

            for (IEditorPart editorPart : editorPool.getEditors(path))
            {

                if (editorPart instanceof ITextEditor)
                {
                    ITextEditor textEditor = (ITextEditor) editorPart;
                    IAnnotationModel model = textEditor.getDocumentProvider()
                            .getAnnotationModel(textEditor.getEditorInput());
                    contributionAnnotationManager.insertAnnotation(model,
                            offset, text.length(), source);
                }
            }
            IAnnotationModel model = provider.getAnnotationModel(input);
            contributionAnnotationManager.insertAnnotation(model, offset,
                    text.length(), source);
        }
        finally
        {
            provider.disconnect(input);
        }
    }

    /**
     * This method verifies if the given EditorPart is supported by Saros, which
     * is based basically on two facts:
     * <p/>
     * 1.) Has a IResource belonging to the project
     * <p/>
     * 2.) Can be mapped to a ITextViewer
     * <p/>
     * Since a null editor does not support either, this method returns false.
     */
    protected boolean isSharedEditor(IEditorPart editorPart)
    {
        if (sarosSession == null)
        {
            return false;
        }

        if (EditorAPI.getViewer(editorPart) == null)
        {
            return false;
        }

        IResource resource = this.editorAPI.getEditorResource(editorPart);

        if (resource == null)
        {
            return false;
        }

        return this.sarosSession.isShared(ResourceAdapterFactory
                .create(resource.getProject()));
    }

    /**
     * Returns <code>true</code> if there is currently a {@link User} followed,
     * otherwise <code>false</code>.
     */
    public boolean isFollowing()
    {
        return getFollowedUser() != null;
    }

    /**
     * Sets the local editor 'opened' and fires an {@link EditorActivity} of
     * type {@link Type#ACTIVATED}.
     *
     * @param path the project-relative path to the resource that the editor is
     *             currently editing or <code>null</code> if the local user has
     *             no editor open.
     */
    public void generateEditorActivated(@Nullable SPath path)
    {

        this.locallyActiveEditor = path;

        if (path != null && sarosSession.isShared(path.getResource()))
        {
            this.locallyOpenEditors.add(path);
        }

        editorListenerDispatch.activeEditorChanged(sarosSession.getLocalUser(),
                path);

        fireActivity(new EditorActivity(sarosSession.getLocalUser(),
                Type.ACTIVATED, path));

    }

    /**
     * Fires an update of the given {@link ITextSelection} for the given
     * {@link IEditorPart} so that all remote parties know that the user
     * selected some text in the given part.
     *
     * @param part         The IEditorPart for which to generate a TextSelectionActivity
     * @param newSelection The ITextSelection in the given part which represents the
     *                     currently selected text in editor.
     */
    public void generateSelection(IEditorPart part, ITextSelection newSelection)
    {

        SPath path = editorAPI.getEditorPath(part);
        if (path == null)
        {
            log.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
        {
            localSelection = newSelection;
        }

        int offset = newSelection.getOffset();
        int length = newSelection.getLength();

        fireActivity(new TextSelectionActivity(sarosSession.getLocalUser(),
                offset, length, path));
    }

    /**
     * Fires an update of the given viewport for the given {@link IEditorPart}
     * so that all remote parties know that the user is now positioned at the
     * given viewport in the given part.
     * <p/>
     * A ViewportActivity not necessarily indicates that the given IEditorPart
     * is currently active. If it is (the given IEditorPart matches the
     * locallyActiveEditor) then the {@link #localViewport} is updated to
     * reflect this.
     *
     * @param part     The IEditorPart for which to generate a ViewportActivity.
     * @param viewport The ILineRange in the given part which represents the
     *                 currently visible portion of the editor. (again visible does
     *                 not mean that this editor is actually the active one)
     */
    public void generateViewport(IEditorPart part, ILineRange viewport)
    {

        if (this.sarosSession == null)
        {
            log.warn("SharedEditorListener not correctly unregistered!");
            return;
        }

        SPath path = editorAPI.getEditorPath(part);
        if (path == null)
        {
            log.warn("Could not find path for editor " + part.getTitle());
            return;
        }

        if (path.equals(locallyActiveEditor))
        {
            this.localViewport = viewport;
        }

        fireActivity(new ViewportActivity(sarosSession.getLocalUser(),
                viewport.getStartLine(), viewport.getNumberOfLines(), path));

        editorListenerDispatch.viewportGenerated(part, viewport, path);

    }


    public void sendEditorActivitySaved(SPath path)
    {
        //todo
        System.out.println("EditorManager.sendEditorActivitySaved");
    }

    @Override
    public void colorChanged()
    {
        //todo
        System.out.println("EditorManager.colorChanged //todo");
    }

    @Override
    public void refreshAnnotations()
    {
        //todo
        System.out.println("EditorManager.refreshAnnotations //todo");
    }

    @Override
    public void setAllLocalOpenedEditorsLocked(boolean locked)
    {
        //todo
        System.out.println("EditorManager.setAllLocalOpenedEditorsLocked //todo");
    }

    @Override
    public SPath[] getOpenEditorsOfAllParticipants()
    {
        //todo
        System.out.println("EditorManager.getOpenEditorsOfAllParticipants //todo");
        return new SPath[0];
    }

    @Override
    public void saveText(SPath path)
    {
        //todo
        System.out.println("EditorManager.saveText //todo");

    }

    public void partClosed(IEditorPart editor)
    {
        //todo
        System.out.println("EditorManager.partClosed //todo");
    }

    public void partInputChanged(IEditorPart editor)
    {
        System.out.println("EditorManager.partInputChanged //todo");
    }

    @Override
    public void exec(IActivity activity)
    {
        System.out.println("EditorManager.exec");
    }

    public static IDocumentProvider getDocumentProvider(IEditorInput input)
    {
        //todo
        System.out.println("EditorManager.getDocumentProvider //todo");
        return null;
    }

    public static IDocument getDocument(IEditorPart editorPart)
    {
        //todo
        System.out.println("EditorManager.getDocument //todo");
        return null;
    }
}
