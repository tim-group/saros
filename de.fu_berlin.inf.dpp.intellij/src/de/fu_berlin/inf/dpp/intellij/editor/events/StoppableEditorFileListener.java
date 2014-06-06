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
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-24
 * Time: 15:28
 */

public class StoppableEditorFileListener extends AbstractStoppableListener implements FileEditorManagerListener {
    private EditorManager manager;
    protected static final Logger log = Logger.getLogger(StoppableEditorFileListener.class);

    public StoppableEditorFileListener(EditorManager manager) {
        this.manager = manager;
        this.enabled = true;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        if (!enabled) {
            return;
        }

        SPath path = manager.getActionManager().toPath(virtualFile);
        if (path != null) {

            if (manager.getActionManager().newFiles.containsKey(virtualFile)) {
                //File is new, need to replace content
                byte[] bytes = new byte[0];
                try {
                    bytes = virtualFile.contentsToByteArray();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

                byte[] bytesRemote = manager.getActionManager().newFiles.get(virtualFile);

                if (!Arrays.equals(bytes, bytesRemote)) {

                    String replacedText = new String(bytes, EncodingProjectManager.getInstance().getDefaultCharset());
                    String text = new String(bytesRemote, EncodingProjectManager.getInstance().getDefaultCharset());

                    manager.generateTextEdit(0, replacedText, text, path);


                }

                manager.getActionManager().newFiles.remove(virtualFile);
            }

            manager.getActionManager().getEditorPool().add(path, fileEditorManager.getSelectedTextEditor());
            manager.getActionManager().startEditor(fileEditorManager.getSelectedTextEditor());

        }
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        if (!enabled) {
            return;
        }


        SPath path = manager.getActionManager().toPath(virtualFile);
        if (path != null) {
            manager.getActionManager().getEditorPool().removeEditor(path);
            manager.generateEditorClosed(path);
        }
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        if (!enabled) {
            return;
        }

        VirtualFile virtualFile = event.getNewFile();
        SPath path = manager.getActionManager().toPath(virtualFile);
        if (path != null) {
            manager.generateEditorActivated(path);
        }

    }

}
