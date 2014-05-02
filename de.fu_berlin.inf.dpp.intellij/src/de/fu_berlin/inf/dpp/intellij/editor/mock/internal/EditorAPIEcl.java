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

package de.fu_berlin.inf.dpp.intellij.editor.mock.internal;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.core.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.mock.Display;
import de.fu_berlin.inf.dpp.intellij.editor.mock.IDE;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ISelectionProvider;
import de.fu_berlin.inf.dpp.intellij.editor.mock.PlatformUI;
import de.fu_berlin.inf.dpp.intellij.editor.mock.events.VerifyEvent;
import de.fu_berlin.inf.dpp.intellij.editor.mock.events.VerifyKeyListener;
import de.fu_berlin.inf.dpp.intellij.editor.mock.exceptions.PartInitException;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.*;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.*;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SarosView;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.WarningMessageDialog;
import de.fu_berlin.inf.dpp.intellij.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * The central implementation of the IEditorAPI which basically encapsulates the
 * interaction with the TextEditor.
 *
 * @author rdjemili
 * @swt Pretty much all methods in this class need to be called from SWT
 */
@Component(module = "core")
public class EditorAPIEcl implements IEditorAPI
{

    private static final Logger log = Logger.getLogger(EditorAPIEcl.class);

    public EditorAPIEcl()
    {
        System.out.println("EditorAPI.EditorAPI");
    }

    protected final VerifyKeyListener keyVerifier = new VerifyKeyListener()
    {
        @Override
        public void verifyKey(VerifyEvent event)
        {
            if (event.character > 0)
            {
                event.doit = false;

                Object adapter = getActiveEditor().getAdapter(
                        IEditorStatusLine.class);
                if (adapter != null)
                {
                    SarosView
                            .showNotification("Read-Only Notification",
                                    "You have only read access and therefore can't perform modifications.");

                    Display display = SWTUtils.getDisplay();

                    if (!display.isDisposed())
                    {
                        display.beep();
                    }
                }
            }
        }
    };

    /**
     * Editors where the user isn't allowed to write
     */
    protected List<IEditorPart> lockedEditors = new ArrayList<IEditorPart>();

    /**
     * Currently managed shared project part listeners for removal by
     * removePartListener
     */
    protected Map<IEditorManager, IPartListener2> partListeners = new HashMap<IEditorManager, IPartListener2>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEditorPartListener(IEditorManager editorManager)
    {
        assert SWTUtils.isSWT();

        if (editorManager == null)
        {
            throw new IllegalArgumentException();
        }

        if (partListeners.containsKey(editorManager))
        {
            log.error("EditorPartListener was added twice: ", new StackTrace());
            removeEditorPartListener(editorManager);
        }

        IPartListener2 partListener = new SafePartListener2(log,
                new EditorPartListener(editorManager));

        partListeners.put(editorManager, partListener);

        // TODO This can fail if a shared project is started when no
        // Eclipse Window is open!
        EditorAPIEcl.getActiveWindow().getPartService()
                .addPartListener(partListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeEditorPartListener(IEditorManager editorManager)
    {

        if (editorManager == null)
        {
            throw new IllegalArgumentException();
        }

        if (!partListeners.containsKey(editorManager))
        {
            throw new IllegalStateException();
        }

        // TODO This can fail if a shared project is started when no
        // Eclipse Window is open!
        IWorkbenchWindow window = EditorAPIEcl.getActiveWindow();
        if (window == null)
        {
            return;
        }

        window.getPartService().removePartListener(
                partListeners.remove(editorManager));
    }

    private boolean warnOnceExternalEditor = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public IEditorPart openEditor(SPath path)
    {
        return openEditor(path, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEditorPart openEditor(SPath path, boolean activate)
    {
        IFile file = path.getFile();

        if (!file.exists())
        {
            log.error("EditorAPI cannot open file which does not exist: "
                    + file, new StackTrace());
            return null;
        }

        IWorkbenchWindow window = EditorAPIEcl.getActiveWindow();
        if (window != null)
        {
            try
            {
                IWorkbenchPage page = window.getActivePage();

                /*
                 * TODO Use
                 *
                 * IWorkbenchPage.openEditor(IEditorInput input, String
                 * editorId, boolean activate)
                 *
                 * to open an editor and set activate to false! So that we can
                 * separate opening from activating, which save us duplicate
                 * sending of activated events.
                 */

                IEditorDescriptor descriptor = IDE.getEditorDescriptor(file);
                if (descriptor.isOpenExternal())
                {
                    /*
                     * WORK-AROUND for #224: Editors are opened externally
                     * erroneously (http://sourceforge.net/p/dpp/bugs/224)
                     *
                     * TODO Open as an internal editor
                     */
                    log.warn("Editor for file " + file.getName()
                            + " is configured to be opened externally,"
                            + " which is not supported by Saros");

                    if (warnOnceExternalEditor)
                    {
                        warnOnceExternalEditor = false;
                        WarningMessageDialog
                                .showWarningMessage(
                                        "Unsupported Editor Settings",
                                        "Eclipse is configured to open this file externally, "
                                                + "which is not supported by Saros.\n\nPlease change the configuration"
                                                + " (Right Click on File -> Open With...) so that the file is opened in Eclipse."
                                                + "\n\nAll further "
                                                + "warnings of this type will be shown in the error "
                                                + "log."
                                );
                    }
                    return null;
                }

                return IDE.openEditor(page, file, activate);
            }
            catch (PartInitException e)
            {
                log.error("Could not initialize part: ", e);
            }
        }

        return null;
    }

    @Override
    public boolean openEditor(IEditorPart part)
    {
        IWorkbenchWindow window = EditorAPIEcl.getActiveWindow();
        if (window == null)
        {
            return false;
        }

        IWorkbenchPage page = window.getActivePage();
        try
        {
            page.openEditor(part.getEditorInput(), part.getEditorSite().getId());
            return true;
        }
        catch (PartInitException e)
        {
            log.error(
                    "failed to open editor part with title: " + part.getTitle(), e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeEditor(IEditorPart part)
    {
        IWorkbenchWindow window = EditorAPIEcl.getActiveWindow();
        if (window != null)
        {
            IWorkbenchPage page = window.getActivePage();
            page.closeEditor(part, true); // Close AND let user decide if saving
            // is necessary
        }
    }

    /**
     * This method will return all editors open in all IWorkbenchWindows.
     * <p/>
     * This method will ask Eclipse to restore editors which have not been
     * loaded yet (if Eclipse is started editors are loaded lazily), which is
     * why it must run in the SWT thread. So calling this method might cause
     * partOpen events to be sent.
     *
     * @return all editors that are currently opened
     * @swt
     */
    public static Set<IEditorPart> getOpenEditors()
    {
        return getOpenEditors(true);
    }

    /**
     * If <code>restore</code> is <code>true</code>, this method will ask
     * Eclipse to restore editors which have not been loaded yet, and must be
     * run in the SWT thread. If false, only editors which are already loaded
     * are returned.
     *
     * @param restore
     * @return
     * @see EditorAPIEcl#getOpenEditors()
     */
    private static Set<IEditorPart> getOpenEditors(boolean restore)
    {
        Set<IEditorPart> editorParts = new HashSet<IEditorPart>();

        IWorkbenchWindow[] windows = EditorAPIEcl.getWindows();
        for (IWorkbenchWindow window : windows)
        {
            IWorkbenchPage[] pages = window.getPages();

            for (IWorkbenchPage page : pages)
            {
                IEditorReference[] editorRefs = page.getEditorReferences();

                for (IEditorReference reference : editorRefs)
                {

                    IEditorPart editorPart = reference.getEditor(false);
                    if (!restore)
                    {
                        continue;
                    }
                    if (editorPart == null)
                    {
                        log.debug("IWorkbenchPage."
                                + "getEditorReferences()"
                                + " returned IEditorPart which needs to be restored: "
                                + reference.getTitle());
                        // Making this call might cause partOpen events
                        editorPart = reference.getEditor(true);
                    }

                    if (editorPart != null)
                    {
                        editorParts.add(editorPart);
                    }
                    else
                    {
                        log.warn("Internal Error: IEditorPart could "
                                + "not be restored: " + reference);
                    }
                }
            }
        }

        return editorParts;
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    public IEditorPart getActiveEditor()
    {
        IWorkbenchWindow window = EditorAPIEcl.getActiveWindow();
        if (window != null)
        {
            IWorkbenchPage page = window.getActivePage();
            if (page != null)
            {
                return page.getActiveEditor();
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IResource getEditorResource(IEditorPart editorPart)
    {

        IEditorInput input = editorPart.getEditorInput();

        IResource resource = ResourceUtil.getResource(input);

        if (resource == null)
        {
            log.warn("Could not get resource from IEditorInput " + input);
        }

        return resource;
    }

    public static int getLine(ITextViewerExtension5 viewer, int offset)
    {
        return viewer.widgetLineOfWidgetOffset(viewer
                .modelOffset2WidgetOffset(offset));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITextSelection getSelection(IEditorPart editorPart)
    {

        if (!(editorPart instanceof ITextEditor))
        {
            return TextSelection.emptySelection();
        }

        ITextEditor textEditor = (ITextEditor) editorPart;
        ISelectionProvider selectionProvider = textEditor
                .getSelectionProvider();
        if (selectionProvider != null)
        {
            return (ITextSelection) selectionProvider.getSelection();
        }

        return TextSelection.emptySelection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEditable(final IEditorPart editorPart,
            final boolean newIsEditable)
    {

        SWTUtils.runSafeSWTSync(log, new Runnable()
        {
            @Override
            public void run()
            {
                ITextViewer textViewer = EditorAPIEcl.getViewer(editorPart);

                if (textViewer == null)
                {
                    return;
                }

                boolean isEditable = !lockedEditors.contains(editorPart);

                // Already as we want it?
                if (newIsEditable == isEditable)
                {
                    return;
                }

                log.trace(editorPart.getEditorInput().getName()
                        + " set to editable: " + newIsEditable);

                updateStatusLine(editorPart, newIsEditable);

                if (newIsEditable)
                {
                    lockedEditors.remove(editorPart);

                    if (textViewer instanceof ITextViewerExtension)
                    {
                        ((ITextViewerExtension) textViewer)
                                .removeVerifyKeyListener(EditorAPIEcl.this.keyVerifier);
                    }

                    // enable editing and undo-manager
                    textViewer.setEditable(true);

                    // TODO use undoLevel from Preferences (TextEditorPlugin)
                    if (textViewer instanceof ITextViewerExtension6)
                    {
                        ((ITextViewerExtension6) textViewer).getUndoManager()
                                .setMaximalUndoLevel(200);
                    }

                }
                else
                {
                    lockedEditors.add(editorPart);

                    if (textViewer instanceof ITextViewerExtension)
                    {
                        ((ITextViewerExtension) textViewer)
                                .prependVerifyKeyListener(EditorAPIEcl.this.keyVerifier);
                    }

                    // disable editing and undo-manager
                    textViewer.setEditable(false);

                    if (textViewer instanceof ITextViewerExtension6)
                    {
                        ((ITextViewerExtension6) textViewer).getUndoManager()
                                .setMaximalUndoLevel(0);
                    }
                }
            }
        });
    }

    /**
     * Map of currently registered EditorListeners for removal via
     * removeSharedEditorListener
     */
    protected Map<Pair<IEditorManager, IEditorPart>, EditorListener> editorListeners = new HashMap<Pair<IEditorManager, IEditorPart>, EditorListener>();

    @Override
    public void addSharedEditorListener(IEditorManager editorManager,
            IEditorPart editorPart)
    {

        assert SWTUtils.isSWT();

        if (editorManager == null || editorPart == null)
        {
            throw new IllegalArgumentException();
        }

        Pair<IEditorManager, IEditorPart> key = new Pair<IEditorManager, IEditorPart>(
                editorManager, editorPart);

        if (editorListeners.containsKey(key))
        {
            log.error(
                    "SharedEditorListener was added twice: "
                            + editorPart.getTitle(), new StackTrace()
            );
            removeSharedEditorListener(editorManager, editorPart);
        }
        EditorListener listener = new EditorListener(editorManager);
        listener.bind(editorPart);
        editorListeners.put(key, listener);
    }

    @Override
    public void removeSharedEditorListener(IEditorManager editorManager,
            IEditorPart editorPart)
    {

        assert SWTUtils.isSWT();

        if (editorManager == null || editorPart == null)
        {
            throw new IllegalArgumentException();
        }

        Pair<IEditorManager, IEditorPart> key = new Pair<IEditorManager, IEditorPart>(
                editorManager, editorPart);

        EditorListener listener = editorListeners.remove(key);
        if (listener == null)
        {
            throw new IllegalArgumentException(
                    "The given editorPart has no EditorListener");
        }

        listener.unbind();
    }

    public static ILineRange getViewport(ITextViewer viewer)
    {

        int top = viewer.getTopIndex();
        // Have to add +1 because a LineRange should excludes the bottom line
        int bottom = viewer.getBottomIndex() + 1;

        if (bottom < top)
        {
            // FIXME This warning occurs when the document is shorter than the
            // viewport
            log.warn("Viewport Range Problem in " + viewer + ": Bottom == "
                    + bottom + " < Top == " + top);
            bottom = top;
        }

        return new LineRange(top, bottom - top);
    }

    @Override
    public ILineRange getViewport(IEditorPart editorPart)
    {

        ITextViewer textViewer = EditorAPIEcl.getViewer(editorPart);
        if (textViewer == null)
        {
            return null;
        }

        return getViewport(textViewer);
    }

    /**
     * Needs UI-thread.
     */
    protected void updateStatusLine(IEditorPart editorPart, boolean editable)
    {
        Object adapter = editorPart.getAdapter(IEditorStatusLine.class);
        if (adapter != null)
        {
            IEditorStatusLine statusLine = (IEditorStatusLine) adapter;
            statusLine.setMessage(false, editable ? "" : "Not editable", null);
        }
    }

    /**
     * @return true when the editor was successfully saved
     * @nonSWT This method may not be called from the SWT UI Thread!
     */
    public static boolean saveEditor(final IEditorPart editor)
    {

        if (editor == null)
        {
            return true;
        }

        final BlockingProgressMonitor monitor = new BlockingProgressMonitor();

        // save document
        SWTUtils.runSafeSWTSync(log, new Runnable()
        {
            @Override
            public void run()
            {
                editor.doSave(monitor);
            }
        });

        // Wait for saving or canceling to be done
        try
        {
            monitor.await();
        }
        catch (InterruptedException e)
        {
            log.warn("Code not designed to handle InterruptedException");
            Thread.currentThread().interrupt();
        }

        return !monitor.isCanceled();
    }

    /**
     * Gets a {@link ITextViewer} instance for the given editor part.
     *
     * @param editorPart for which we want a {@link TextViewer}.
     * @return {@link ITextViewer} or <code>null</code> if there is no
     * {@link TextViewer} for the editorPart.
     */
    public static ITextViewer getViewer(IEditorPart editorPart)
    {

        Object viewer = editorPart.getAdapter(ITextOperationTarget.class);
        if (viewer instanceof ITextViewer)
        {
            return (ITextViewer) viewer;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the active workbench window. Needs to be called from UI thread.
     *
     * @return the active workbench window or <code>null</code> if there is no
     * window or method is called from non-UI thread or the
     * activeWorkbenchWindow is disposed.
     * @see IWorkbench#getActiveWorkbenchWindow()
     */
    protected static IWorkbenchWindow getActiveWindow()
    {
        try
        {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    protected static IWorkbenchWindow[] getWindows()
    {
        return PlatformUI.getWorkbench().getWorkbenchWindows();
    }

    /**
     * Saves the given project and returns true if the operation was successful
     * or false if the user canceled.
     * <p/>
     * TODO Tell the user why we do want to save!
     *
     * @param confirm true to ask the user before saving unsaved changes, and false
     *                to save unsaved changes without asking
     */
    public static boolean saveProject(final IProject projectToSave,
            final boolean confirm)
    {
        try
        {
            Boolean result = true;
            result = SWTUtils.runSWTSync(new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {
                    /**
                     * TODO saveAllEditors does not save the Documents that we
                     * are modifying in the background
                     */
                    return IDE.saveAllEditors(
                            new IResource[]{projectToSave}, confirm);
                }
            });
            return result;
        }
        catch (Exception e)
        {
            // The operation does not throw an exception, thus this is an error.
            throw new RuntimeException(e);
        }
    }

    public boolean existUnsavedFiles(IProject proj)
    {
        // Note: We don't need to check editors that aren't loaded.
        for (IEditorPart editorPart : getOpenEditors(false))
        {
            final IEditorInput editorInput = editorPart.getEditorInput();
            if (!(editorInput instanceof IFileEditorInput))
            {
                continue;
            }
            // Object identity instead of using equals is sufficient.
            IFile file = ((IFileEditorInput) editorInput).getFile();
            if (file.getProject() == proj && editorPart.isDirty())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Syntactic sugar for getting the path of the IEditorPart returned by
     * getActiveEditor()
     */
    @Override
    public SPath getActiveEditorPath()
    {
        IEditorPart newActiveEditor = getActiveEditor();
        if (newActiveEditor == null)
        {
            return null;
        }

        return getEditorPath(newActiveEditor);
    }

    /**
     * Syntactic sugar for getting the path of the given editor part.
     */
    @Override
    public SPath getEditorPath(IEditorPart editorPart)
    {
        IResource resource = getEditorResource(editorPart);
        if (resource == null)
        {
            return null;
        }
        IPath path = resource.getProjectRelativePath();

        if (path == null)
        {
            log.warn("Could not get path from resource " + resource);
        }

        return new SPath(ResourceAdapterFactory.create(resource));
    }
}
