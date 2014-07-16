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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import de.fu_berlin.inf.dpp.core.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableDocumentListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableEditorFileListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableSelectionListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableViewPortListener;
import de.fu_berlin.inf.dpp.intellij.editor.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.intellij.project.fs.ResourceConverter;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for executing actions on IntelliJ editors that come form remote and for handling actions
 * that were executed locally.
 */
//FIXME: Split the EditorManipulator into seperate classes for actions coming from local and those from remote
public class EditorManipulator {

    private Logger LOG = Logger.getLogger(EditorManipulator.class);

    private final ProjectAPI projectAPI;
    private final EditorAPI editorAPI;

    /**
     * This is just a reference to {@link de.fu_berlin.inf.dpp.core.editor.EditorManager}'s editorPool and not a
     * separate pool.
     */
    private EditorPool editorPool;

    private EditorManager manager;

    private StoppableDocumentListener documentListener;
    private StoppableEditorFileListener fileListener;
    private StoppableSelectionListener selectionListener;
    private StoppableViewPortListener viewportListener;
    private Map<VirtualFile, byte[]> newFiles = new HashMap<VirtualFile, byte[]>();

    public EditorManipulator(ProjectAPI projectAPI, EditorAPI editorAPI) {
        this.projectAPI = projectAPI;
        this.editorAPI = editorAPI;
    }

    /**
     * Initializes all fields that require an EditorManager.
     *
     * @param manager
     */
    public void setEditorManager(EditorManager manager) {
        this.editorPool = manager.getEditorPool();
        this.manager = manager;
        this.documentListener = new StoppableDocumentListener(manager);

        this.fileListener = new StoppableEditorFileListener(manager);
        this.selectionListener = new StoppableSelectionListener(manager);
        this.viewportListener = new StoppableViewPortListener(manager);
        projectAPI.addFileEditorManagerListener(this.fileListener);
    }


    /**
     * Opens an editor for the given path, if it exists.
     *
     * @param path
     * @return the editor for the given path
     */
    public Editor openEditorFromRemote(SPath path) {

        VirtualFile virtualFile = ResourceConverter.toVirtualFile(path);
        if (virtualFile.exists()) {
            //todo: in case it is already open, need to activate only, not open
            Editor editor = projectAPI.openEditor(virtualFile);
            startEditor(editor);
            editorPool.add(path, editor);
            return editor;
        } else {
            LOG.warn("File not exist " + path);
        }
        return null;
    }

    /**
     * Adds the opened file to the editorPool and calls {@link #startEditor(com.intellij.openapi.editor.Editor)} on the
     * opened Editor. Additionally it sends the file's content via
     * {@link EditorManager#generateTextEdit(int, String, String, de.fu_berlin.inf.dpp.activities.SPath)},
     * when it is newly created local file.
     *
     * @param virtualFile
     */
    public void openEditorFromLocal(VirtualFile virtualFile) {
        SPath path = toPath(virtualFile);
        if (path != null) {

            if (newFiles.containsKey(virtualFile)) {
                //File is new, need to replaceAll content
                byte[] bytes = new byte[0];
                try {
                    bytes = virtualFile.contentsToByteArray();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }

                byte[] bytesRemote = newFiles.get(virtualFile);

                if (!Arrays.equals(bytes, bytesRemote)) {

                    String replacedText = new String(bytes, EncodingProjectManager.getInstance().getDefaultCharset());
                    String text = new String(bytesRemote, EncodingProjectManager.getInstance().getDefaultCharset());

                    manager.generateTextEdit(0, replacedText, text, path);
                }

                newFiles.remove(virtualFile);
            }

            editorPool.add(path, projectAPI.getActiveEditor());
            startEditor(projectAPI.getActiveEditor());
        }
    }

    /**
     * Removes a file from the editorPool and calls
     * {@link de.fu_berlin.inf.dpp.core.editor.EditorManager#generateEditorClosed(de.fu_berlin.inf.dpp.activities.SPath)}
     *
     * @param virtualFile
     */
    public void closeEditorFromLocal(VirtualFile virtualFile) {
        SPath path = toPath(virtualFile);
        if (path != null) {
            editorPool.removeEditor(path);
            manager.generateEditorClosed(path);
        }
    }

    /**
     * Closes the editor under path.
     *
     * @param path
     */
    public void closeEditorFromRemote(SPath path) {
        VirtualFile virtualFile = ResourceConverter.toVirtualFile(path);
        if (virtualFile != null && virtualFile.exists()) {
            if (projectAPI.isOpen(virtualFile)) {
                Document doc = editorPool.getDocument(path);
                if (doc != null) {
                    documentListener.setDocument(null);
                }

                projectAPI.closeEditor(virtualFile);
            }
            editorPool.removeEditor(path);

        } else {
            LOG.warn("File not exist " + path);
        }
    }

    /**
     * Saves the document under path..
     *
     * @param path
     */
    public void saveFile(SPath path) {
        Document doc = editorPool.getDocument(path);
        if (doc != null) {
            projectAPI.saveDocument(doc);
        } else {
            LOG.warn("Document does not exist: " + path);
        }
    }

    /**
     * Replaces the complete text at the given path.
     *
     * @param path
     * @param text
     * @return Returns true if replacement was successful
     */
    public boolean replaceText(SPath path, String text) {
        Document doc = editorPool.getDocument(path);
        if (doc != null) {
            boolean bWritable = doc.isWritable();
            doc.setReadOnly(false);
            editorAPI.setText(doc, text);
            doc.setReadOnly(!bWritable);
            return true;
        }

        return false;
    }

    /**
     * Executes the received operations on the path.
     *
     * @param path
     * @param operations
     * @param color
     */
    public void editTextFromRemote(SPath path, Operation operations, Color color) {
        Document doc = editorPool.getDocument(path);
        if (doc == null) {
            VirtualFile virtualFile = ResourceConverter.toVirtualFile(path);
            doc = projectAPI.createDocument(virtualFile);
            editorPool.add(path, doc);
        }

         /*
         * Disable documentListener temporarily to avoid being notified of the
         * change
         */
        documentListener.setEnabled(false);
        for (ITextOperation op : operations.getTextOperations()) {
            if (op instanceof DeleteOperation) {
                editorAPI.deleteText(doc, op.getPosition(), op.getPosition() + op.getTextLength());
            } else {
                editorAPI.insertText(doc, op.getPosition(), op.getText());
                Editor editor = editorPool.getEditor(path);
                if (editor != null) {
                    editorAPI.textMarkAdd(editor, op.getPosition(), op.getPosition() + op.getTextLength(), color);
                }
            }
        }

        documentListener.setEnabled(true);
    }

    /**
     * Calls {@link EditorManager#generateEditorActivated(de.fu_berlin.inf.dpp.activities.SPath)}.
     *
     * @param file
     */
    public void activateEditorFromLocal(VirtualFile file) {
        SPath path = toPath(file);
        if (path != null) {
            manager.generateEditorActivated(path);
        }
    }

    /**
     * Removes all text marks from the path.
     *
     * @param path
     */
    public void clearSelectionFromRemote(SPath path) {
        Editor editor = editorPool.getEditor(path);
        if (editor != null) {
            editorAPI.textMarkRemove(editor, null);
        }
    }

    /**
     * Selects the specified range for the editor of the given path.
     *
     * @param path
     * @param position
     * @param length
     * @param colorModel
     */
    public void selectTextFromRemote(SPath path, int position, int length, ColorModel colorModel) {
        Editor editor = editorPool.getEditor(path);
        if (editor != null) {
            editorAPI.textMarkRemove(editor, colorModel.getSelect());
            RangeHighlighter highlighter = editorAPI.textMarkAdd(editor, position, position + length, colorModel.getSelectColor());
            colorModel.setSelect(highlighter);
            //editorAPI.setSelection(editor, position, position + length,color); //todo: calculate new line char win and unix differences

        }
    }

    /**
     * Sets the viewport of the editor for path to the specified range.
     *
     * @param path
     * @param lineStart
     * @param lineEnd
     */
    public void setViewPortFromRemote(final SPath path, final int lineStart, final int lineEnd) {
        Editor editor = editorPool.getEditor(path);
        if (editor != null) {
            editorAPI.setViewPort(editor, lineStart, lineEnd);
        }
    }

    /**
     * Adjusts viewport. Focus is set on the center of the range, but priority
     * is given to selected lines.
     *
     * @param editor    Editor of the open Editor
     * @param range     viewport of the followed user. Must not be <code>null</code>.
     * @param selection text selection of the followed user. Must not be <code>null</code>.
     */
    public void adjustViewport(Editor editor, LineRange range, TextSelection selection) {
        if (editor == null || selection == null || range == null) {
            return;
        }

        editorAPI.setSelection(editor, selection.getOffset(), selection.getOffset() + selection.getLength(), null);
        editorAPI.setViewPort(editor, range.getStartLine(), range.getStartLine() + range.getNumberOfLines());

        //todo: implement actual viewport adjustment logic
    }

    /**
     * Unlocks all editors in the editorPool.
     */
    public void unlockAllEditors() {
        enableListeners(true);
        for (Document doc : editorPool.getDocuments()) {
            doc.setReadOnly(false);
        }
    }

    /**
     * Locks all open editors, by setting them to read-only.
     */
    public void lockAllEditors() {
        enableListeners(false);

        for (Document doc : editorPool.getDocuments()) {
            doc.setReadOnly(true);
        }
    }

    /**
     * Unlocks all locally open editors by starting them.
     */
    public void unlockAllLocalOpenedEditors() {
        for (Editor editor : editorPool.getEditors()) {
            startEditor(editor);
        }
    }

    /**
     * Enables all listener if the parameter is <code>true</code>, else disables them.
     *
     * @param enable
     */
    public void enableListeners(boolean enable) {

        if (documentListener != null) {
            documentListener.setEnabled(enable);
        }

        if (fileListener != null) {
            fileListener.setEnabled(enable);
        }

        if (selectionListener != null) {
            selectionListener.setEnabled(enable);
        }

        if (viewportListener != null) {
            viewportListener.setEnabled(enable);
        }
    }

    /**
     * Adds a newly created file to the newFile list for sending it when it is opened.
     *
     * @param virtualFile
     * @param content
     */
    public void registerNewFile(VirtualFile virtualFile, byte[] content) {
        newFiles.put(virtualFile, content);
    }

    private void startEditor(Editor editor) {
        editor.getDocument().setReadOnly(false);
        editor.getSelectionModel().addSelectionListener(selectionListener);
        editor.getScrollingModel().addVisibleAreaListener(viewportListener);
        documentListener.setDocument(editor.getDocument());
    }

    private void stopEditor(Editor editor) {
        editor.getSelectionModel().removeSelectionListener(selectionListener);
        editor.getScrollingModel().removeVisibleAreaListener(viewportListener);
        documentListener.setDocument(null);
    }

    private SPath toPath(VirtualFile virtualFile) {
        if (virtualFile == null || !virtualFile.exists() || !manager.hasSession()) {
            return null;
        }

        IResource resource = null;
        String path = virtualFile.getPath();

        for (IProject project : manager.getSession().getProjects()) {
            resource = project.getFile(path);
            if (resource != null) {
                break;
            }

        }
        return resource == null ? null : new SPath(resource);
    }

    public boolean isOpenEditor(SPath path) {
        Document doc = editorPool.getDocument(path);
        if (doc == null) {
            return false;
        }

        return projectAPI.isOpen(doc);
    }
}
