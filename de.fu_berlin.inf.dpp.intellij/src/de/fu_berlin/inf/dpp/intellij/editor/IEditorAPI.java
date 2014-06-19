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


import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.editor.IEditorManagerBase;
import de.fu_berlin.inf.dpp.filesystem.IProject;


/**
 * A humble interface that is responsible for editor functionality. The idea
 * behind this interface is to only capsulates the least possible amount of
 * functionality - the one that can't be easily tested. All higher logic can be
 * found in {@link de.fu_berlin.inf.dpp.intellij.editor.mock.eclipse.EditorManagerEcl}.
 *
 * @author rdjemili
 */
public interface IEditorAPI {


    /**
     * Syntactic sugar for getting the path of the IEditorPart returned by
     * getActiveEditor()
     */
    public SPath getActiveEditorPath();


    /**
     * Removes a previously registered PartListener added via
     * {@link #addEditorPartListener(de.fu_berlin.inf.dpp.intellij.editor.mock.eclipse.EditorManagerEcl)}.
     *
     * @throws IllegalArgumentException if the EditorManagerEcl is null
     * @throws IllegalStateException    if the given EditorManagerEcl has never been registered via
     *                                  {@link #addEditorPartListener(de.fu_berlin.inf.dpp.intellij.editor.mock.eclipse.EditorManagerEcl)}
     * @swt Needs to be called from the SWT-UI thread.
     */
    public void removeEditorPartListener(IEditorManagerBase editorManager);

    /**
     * Register a PartListener on the currently active WorkbenchWindow using the
     * given EditorManagerEcl as callback.
     * <p/>
     * If a part listener is already registered for the given editorManager it
     * is removed before adding a new listener (but a warning will be printed!)
     *
     * @throws IllegalArgumentException if the EditorManagerEcl is null
     * @swt Needs to be called from the SWT-UI thread.
     */
    public void addEditorPartListener(IEditorManagerBase editorManager);

    public boolean existUnsavedFiles(IProject project);
}
