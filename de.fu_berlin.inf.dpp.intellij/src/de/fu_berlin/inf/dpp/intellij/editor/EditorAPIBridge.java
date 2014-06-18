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
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-22
 * Time: 13:39
 */

public class EditorAPIBridge  implements IEditorAPI
{
    private EditorAPI editorAPI;


    @Override
    public IEditorPart openEditor(SPath path)
    {
        return null;
    }

    @Override
    public IEditorPart openEditor(SPath path, boolean activate)
    {
        return null;
    }

    @Override
    public boolean openEditor(IEditorPart part)
    {
        return false;
    }

    @Override
    public void closeEditor(IEditorPart part)
    {

    }


    @Override
    public ITextSelection getSelection(IEditorPart editorPart)
    {
        return null;
    }

    @Override
    public SPath getEditorPath(IEditorPart editorPart)
    {
        return null;
    }

    @Override
    public ILineRange getViewport(IEditorPart editorPart)
    {
        return null;
    }

    @Override
    public void setEditable(IEditorPart editorPart, boolean editable)
    {

    }

    @Override
    public void addSharedEditorListener(IEditorManagerBase editorManager, IEditorPart editorPart) {

    }

    @Override
    public void removeSharedEditorListener(IEditorManagerBase editorManager, IEditorPart editorPart) {

    }


    @Override
    public SPath getActiveEditorPath()
    {
        return null;
    }

    @Override
    public IResource getEditorResource(IEditorPart editorPart)
    {
        return null;
    }

    @Override
    public void removeEditorPartListener(IEditorManagerBase editorManager) {

    }

    @Override
    public void addEditorPartListener(IEditorManagerBase editorManager) {

    }


    @Override
    public boolean existUnsavedFiles(IProject project)
    {
        return false;
    }
}
