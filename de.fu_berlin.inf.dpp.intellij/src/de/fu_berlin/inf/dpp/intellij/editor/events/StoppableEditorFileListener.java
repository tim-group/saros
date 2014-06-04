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
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-24
 * Time: 15:28
 */

public class StoppableEditorFileListener extends AbstractStoppableListener implements FileEditorManagerListener
{
    private EditorManager manager;


    public StoppableEditorFileListener(EditorManager manager)
    {
        this.manager = manager;
        this.enabled = true;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile)
    {
        if (!enabled)
        {
            return;
        }

        SPath path = manager.getActionManager().toPath(virtualFile);
        if (path != null)
        {
              manager.getActionManager().getEditorPool().add(path,fileEditorManager.getSelectedTextEditor());
              manager.getActionManager().startEditor(fileEditorManager.getSelectedTextEditor());

            // manager.generateEditorActivated(path);  //no need to fire event
        }
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile)
    {
        if (!enabled)
        {
            return;
        }


        SPath path = manager.getActionManager().toPath(virtualFile);
        if (path != null)
        {
            // manager.getActionManager().getEditorPool().remove(path);
             manager.generateEditorClosed(path);
        }
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event)
    {
        if (!enabled)
        {
            return;
        }

        VirtualFile virtualFile = event.getNewFile();
        SPath path = manager.getActionManager().toPath(virtualFile);
        if (path != null)
        {
            manager.generateEditorActivated(path);
        }

    }

}
