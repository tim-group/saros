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

package de.fu_berlin.inf.dpp.core.editor.internal;



import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.editor.IEditorManager;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;


/**
 * A humble interface that is responsible for editor functionality. The idea
 * behind this interface is to only capsulates the least possible amount of
 * functionality - the one that can't be easily tested. All higher logic can be
 * found in {@link EditorManager}.
 *
 * @author rdjemili
 */
public interface IEditorAPI {

    /**
     * Opens the editor with given path. Needs to be called from an UI thread.
     *
     * @return the opened editor or <code>null</code> if the editor couldn't be
     *         opened.
     */
    public IEditorPart openEditor(SPath path);

    /**
     * Opens the editor with given path. Needs to be called from an UI thread.
     *
     * @param activate
     *            <code>true</code>, if editor should get focus, otherwise
     *            <code>false</code>
     * @return the opened editor or <code>null</code> if the editor couldn't be
     *         opened.
     */
    public IEditorPart openEditor(SPath path, boolean activate);

    /**
     * Opens the given editor part.
     *
     * Needs to be called from an UI thread.
     *
     * @return <code>true</code> if the editor part was successfully opened,
     *         <code>false</code> otherwise
     */
    public boolean openEditor(IEditorPart part);

    /**
     * Closes the given editorpart.
     *
     * Needs to be called from an UI thread.
     */
    public void closeEditor(IEditorPart part);

    /**
     * @return the editor that is currently activated.
     */
    public IEditorPart getActiveEditor();

    /**
     * Returns the current text selection for given editor.
     *
     * @param editorPart
     *            the editorPart for which to get the text selection.
     * @return the current text selection. Returns
     *         {@link TextSelection#emptySelection()} if no text selection
     *         exists.
     *
     */
    public ITextSelection getSelection(IEditorPart editorPart);

    /**
     * @return the path of the file the given editor is displaying or null if
     *         the given editor is not showing a file or the file is not
     *         referenced via a path in the project.
     */
    public SPath getEditorPath(IEditorPart editorPart);

    /**
     * @return Return the viewport for given editor or null, if this editorPart
     *         does not have ITextViewer associated.
     */
    public ILineRange getViewport(IEditorPart editorPart);

    /**
     * Enables/disables the ability to edit the document in given editor.
     */
    public void setEditable(IEditorPart editorPart, boolean editable);

    /**
     * Attaches listeners to the given editor that will fire the
     * {@link EditorListener} methods on the given editor manager
     *
     * Connecting to an editorPart multiple times, will automatically remove
     * previous listeners via removeSharedEditorListener(IEditorPart editorPart)
     * (but will print a warning!)
     *
     * @swt Needs to be called from the SWT-UI thread.
     *
     * @throws IllegalArgumentException
     *             if the given editorPart does not have an ITextViewer or if
     *             the EditorManager or EditorPart are null
     *
     */
    public void addSharedEditorListener(IEditorManager editorManager,
            IEditorPart editorPart);

    /**
     * Removes the listener to the given editor for the given manager previously
     * added via {@link #addSharedEditorListener(EditorManager, IEditorPart)}.
     *
     * @swt Needs to be called from the SWT-UI thread.
     *
     * @throws IllegalArgumentException
     *             if the EditorManager or EditorPart are null
     *
     * @throws IllegalStateException
     *             if the given editorPart has never been registered via
     *             {@link #addSharedEditorListener(EditorManager, IEditorPart)}.
     */
    public void removeSharedEditorListener(IEditorManager editorManager,
            IEditorPart editorPart);

    /**
     * Syntactic sugar for getting the path of the IEditorPart returned by
     * getActiveEditor()
     */
    public SPath getActiveEditorPath();

    /**
     * Returns the resource currently displayed in the given editorPart, this
     * maybe null if the given editor is not operating on a resource or has
     * several resources (TeamEditor for instance).
     */
    public IResource getEditorResource(IEditorPart editorPart);

    /**
     * Removes a previously registered PartListener added via
     * {@link #addEditorPartListener(EditorManager)}.
     *
     * @swt Needs to be called from the SWT-UI thread.
     *
     * @throws IllegalArgumentException
     *             if the EditorManager is null
     *
     * @throws IllegalStateException
     *             if the given EditorManager has never been registered via
     *             {@link #addEditorPartListener(EditorManager)}
     */
    public void removeEditorPartListener(IEditorManager editorManager);

    /**
     * Register a PartListener on the currently active WorkbenchWindow using the
     * given EditorManager as callback.
     *
     * If a part listener is already registered for the given editorManager it
     * is removed before adding a new listener (but a warning will be printed!)
     *
     * @swt Needs to be called from the SWT-UI thread.
     *
     * @throws IllegalArgumentException
     *             if the EditorManager is null
     */
    public void addEditorPartListener(IEditorManager editorManager);

    public boolean existUnsavedFiles(IProject project);
}
