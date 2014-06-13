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

package de.fu_berlin.inf.dpp.intellij.mock.editor.internal;


import de.fu_berlin.inf.dpp.intellij.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.IWorkbenchPart;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.IWorkbenchPartReference;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.IPartListener2;

public class EditorPartListener implements IPartListener2
{

    protected IEditorManager editorManager;

    public EditorPartListener(IEditorManager editorManager) {
        this.editorManager = editorManager;
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partActivated(editor);
        }
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partOpened(editor);
        }
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partClosed(editor);
        }
    }

    /**
     * We need to catch partBroughtToTop events because partActivate events are
     * missing if Editors are opened programmatically.
     */
    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partActivated(editor);
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // do nothing
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        // do nothing
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        // do nothing
    }

    /**
     * Called for instance when a file was renamed. We just close and open the
     * editor.
     */
    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);

        if ((part != null) && (part instanceof IEditorPart)) {
            IEditorPart editor = (IEditorPart) part;
            editorManager.partInputChanged(editor);
        }
    }
}