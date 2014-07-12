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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableEditorFileListener;

/**
 * IntellIJ API for project-level operations on editors and documents.
 */
public class ProjectAPI {


    private Application application;
    private FileDocumentManager fileDocumentManager;

    private Project project;
    protected FileEditorManager editorFileManager;

    /**
     * Creates an EditorAPI with the current Project.
     */
    public ProjectAPI() {
        this.project = Saros.getInstance().getProject();
        this.editorFileManager = FileEditorManager.getInstance(project);

        this.application = ApplicationManager.getApplication();
        this.fileDocumentManager = FileDocumentManager.getInstance();
    }


    public boolean isOpen(VirtualFile file) {
        return editorFileManager.isFileOpen(file);
    }

    public boolean isOpen(Document doc) {
        VirtualFile file = fileDocumentManager.getFile(doc);
        return isOpen(file);
    }

    class EditorContainer {
        Editor editor;
    }


    /**
     * Opens an editor for the given path in the UI thread.
     *
     * @param path
     * @return
     */
    public Editor openEditor(final VirtualFile path) {

        final EditorContainer result = new EditorContainer();

        Runnable action = new Runnable() {
            @Override
            public void run() {

                application.runReadAction(new Runnable() {
                    @Override
                    public void run() {
                        editorFileManager.openFile(path, true);

                        result.editor = editorFileManager.getSelectedTextEditor();
                    }
                });

            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);

        return result.editor;

    }

    public Document createDocument(VirtualFile path) {
        return fileDocumentManager.getDocument(path);
    }


    public void closeEditor(final VirtualFile file) {

        Runnable action = new Runnable() {
            @Override
            public void run() {
                editorFileManager.closeFile(file);
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);
    }


    public void closeEditor(Document doc) {
        VirtualFile file = fileDocumentManager.getFile(doc);
        closeEditor(file);
    }

    public Editor getActiveEditor() {
        return editorFileManager.getSelectedTextEditor();
    }

    public void saveDocument(final Document doc) {
        application.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                application.runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        fileDocumentManager.saveDocument(doc);
                    }
                });
            }
        }, ModalityState.NON_MODAL);

    }


    public void reloadFromDisk(final Document doc) {
        application.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                application.runReadAction(new Runnable() {
                    @Override
                    public void run() {
                        fileDocumentManager.reloadFromDisk(doc);
                    }
                });
            }
        }, ModalityState.NON_MODAL);
    }

    public void saveAllDocuments() {
        application.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                application.runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        fileDocumentManager.saveAllDocuments();
                    }
                });
            }
        }, ModalityState.NON_MODAL);

    }

    public void addFileEditorManagerListener(StoppableEditorFileListener listener) {
        if (editorFileManager != null) {
            editorFileManager.addFileEditorManagerListener(listener);
        }

    }
}
