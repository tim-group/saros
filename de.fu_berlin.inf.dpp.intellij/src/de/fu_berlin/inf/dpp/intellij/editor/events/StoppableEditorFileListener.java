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

package de.fu_berlin.inf.dpp.intellij.editor.events;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.core.editor.EditorManager;
import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ editor file listener
 */
public class StoppableEditorFileListener extends AbstractStoppableListener implements FileEditorManagerListener {

    public StoppableEditorFileListener(EditorManager manager) {
        super(manager);
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        if (!enabled) {
            return;
        }

        editorManager.getEditorManipulator().openEditorFromLocal(virtualFile);
    }


    @Override
    public void fileClosed(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        if (!enabled) {
            return;
        }

        editorManager.getEditorManipulator().closeEditorFromLocal(virtualFile);
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        if (!enabled) {
            return;
        }

        editorManager.getEditorManipulator().activateEditorFromLocal(event.getNewFile());
    }
}
