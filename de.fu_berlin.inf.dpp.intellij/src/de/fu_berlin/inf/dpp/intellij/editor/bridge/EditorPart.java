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

package de.fu_berlin.inf.dpp.intellij.editor.bridge;

import com.intellij.openapi.editor.Editor;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorInput;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-05
 * Time: 18:01
 */

public class EditorPart implements IEditorPart
{
    private Editor editor;
    private EditorInput input;

    public EditorPart(Editor editor)
    {
        this.editor = editor;
        this.input = new EditorInput();
    }

    @Override
    public int getId()
    {
        return 0;
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    @Override
    public boolean contains(IEditorPart editorPart)
    {
        return false;
    }

    @Override
    public IEditorInput getEditorInput()
    {
        return null;
    }

    @Override
    public boolean isDirty()
    {
        return false;
    }

    @Override
    public Object getAdapter(Class clazz)
    {
        return null;
    }


    @Override
    public void doSave(IProgressMonitor monitor) {

    }

    @Override
    public IEditorPart getEditorSite()
    {
        return null;
    }
}
