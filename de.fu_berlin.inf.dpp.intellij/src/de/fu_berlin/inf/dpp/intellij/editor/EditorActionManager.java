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
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import de.fu_berlin.inf.dpp.core.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.adapter.DocumentProvider;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableDocumentListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableEditorFileListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableSelectionListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableViewPortListener;
import de.fu_berlin.inf.dpp.intellij.editor.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.editor.text.TextSelection;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Editor action manager. Class
 */
public class EditorActionManager {
    private final ProjectAPI projectAPI;
    private Logger LOG = Logger.getLogger(EditorActionManager.class);
    private EditorPool editorPool;
    private EditorAPI editorAPI;

    private EditorManager manager;

    private StoppableDocumentListener documentListener;
    private StoppableEditorFileListener fileListener;
    private StoppableSelectionListener selectionListener;
    private StoppableViewPortListener viewportListener;
    private DocumentProvider adapter;
    public Map<VirtualFile, byte[]> newFiles = new HashMap<VirtualFile, byte[]>();

    private LocalFileSystem localFileSystem;
    private FileDocumentManager fileDocumentManager;

    public EditorActionManager(EditorManager manager) {
        this.editorPool = new EditorPool();
        this.editorAPI = new EditorAPI();
        this.projectAPI = new ProjectAPI();
        this.manager = manager;

        this.documentListener = new StoppableDocumentListener(manager);

        this.fileListener = new StoppableEditorFileListener(manager);
        this.selectionListener = new StoppableSelectionListener(manager);
        this.viewportListener = new StoppableViewPortListener(manager);
        this.adapter = new DocumentProvider(this);

        this.localFileSystem = LocalFileSystem.getInstance();
        this.fileDocumentManager = FileDocumentManager.getInstance();

        projectAPI.addFileEditorManagerListener(this.fileListener);
    }


    public Editor openEditor(SPath file) {

        VirtualFile virtualFile = toVirtualFile(file);
        if (virtualFile.exists()) {
            // this.fileListener.setEnabled(false);

            if (projectAPI.isOpen(virtualFile)) {
                Editor editor = projectAPI.openEditor(virtualFile);   //todo: need to activate only, not open!


                startEditor(editor);

                editorPool.add(file, editor);

                return editor;

                //  return editorPool.getEditor(file);
                //  editorFileManager.setSelectedEditor(path,FileEditorProvider.getEditorTypeId());
            } else {
                Editor editor = projectAPI.openEditor(virtualFile);

//            DocumentAdapter pooledDoc = editorPool.getDocument(file);
//            if (pooledDoc != null)
//            {
//            //    pooledDoc.removeDocumentListener(documentListener);
//                editorPool.removeAll(file);
//            }

                // editor.getDocument().addDocumentListener(documentListener);
                // documentListener.setDocument(editor.getDocument());
                startEditor(editor);


                editorPool.add(file, editor);

                //this.fileListener.setEnabled(true);

                return editor;
            }
        } else {
            LOG.warn("File not exist " + file);
        }

        return null;
    }

    public void startEditor(Editor editor) {
        editor.getDocument().setReadOnly(false);
        editor.getSelectionModel().addSelectionListener(selectionListener);
        editor.getScrollingModel().addVisibleAreaListener(viewportListener);
        documentListener.setDocument(editor.getDocument());
    }

    public void stopEditor(Editor editor) {
        editor.getSelectionModel().removeSelectionListener(selectionListener);
        editor.getScrollingModel().removeVisibleAreaListener(viewportListener);
        documentListener.setDocument(null);
    }

    /**
     * Returns true ir replacement done
     *
     * @param path
     * @param text
     * @return
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

    public void closeEditor(SPath file) {
        VirtualFile virtualFile = toVirtualFile(file);
        if (virtualFile != null && virtualFile.exists()) {
            //   this.fileListener.setEnabled(false);
            if (projectAPI.isOpen(virtualFile)) {
                Document doc = editorPool.getDocument(file);
                if (doc != null) {
                    // doc.removeDocumentListener(documentListener);
                    documentListener.setDocument(null);
                }

                projectAPI.closeEditor(virtualFile);
            }
            editorPool.removeEditor(file);

            //      this.fileListener.setEnabled(true);
        } else {
            LOG.warn("File not exist " + file);
        }
    }

    public void saveEditor(SPath file) {
        Document doc = editorPool.getDocument(file);
        if (doc != null) {
            // this.fileListener.setEnabled(false);
            projectAPI.saveDocument(doc);
            // this.fileListener.setEnabled(true);
        } else {
            LOG.warn("DocumentAdapter not exist " + file);
        }
    }

    public void editText(SPath file, Operation operations, Color color) {
        Document doc = editorPool.getDocument(file);
        if (doc == null) {
            VirtualFile virtualFile = toVirtualFile(file);
            doc = projectAPI.createDocument(virtualFile);
            editorPool.add(file, doc);
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
                Editor editor = editorPool.getEditor(file);
                if (editor != null) {
                    editorAPI.textMarkAdd(editor, op.getPosition(), op.getPosition() + op.getTextLength(), color);
                }
            }
        }

        documentListener.setEnabled(true);
    }

    public void clearSelection(SPath file) {
        Editor editor = editorPool.getEditor(file);
        if (editor != null) {
            editorAPI.textMarkRemove(editor, null);
        }
    }

    public void selectText(SPath file, int position, int length, ColorModel colorModel) {
        Editor editor = editorPool.getEditor(file);
        if (editor != null) {
            editorAPI.textMarkRemove(editor, colorModel.getSelect());
            RangeHighlighter highlighter = editorAPI.textMarkAdd(editor, position, position + length, colorModel.getSelectColor());
            colorModel.setSelect(highlighter);
            //editorAPI.setSelection(editor, position, position + length,color); //todo: calculate new line char win and unix differences

        }
    }

    public void setViewPort(final SPath file, final int lineStart, final int lineEnd) {
        Editor editor = editorPool.getEditor(file);
        if (editor != null) {
            editorAPI.setViewPort(editor, lineStart, lineEnd);
        }
    }

    /**
     * Adjusts viewport. Focus is set on the center of the range, but priority
     * is given to selected lines. Either range or selection can be null, but
     * not both.
     *
     * @param editor    Editor of the open Editor
     * @param range     viewport of the followed user. Can be <code>null</code>.
     * @param selection text selection of the followed user. Can be <code>null</code>.
     */
    public void adjustViewport(Editor editor, LineRange range, TextSelection selection) {
        if (editor == null || selection == null || range == null) {
            return;
        }


        editorAPI.setSelection(editor, selection.getOffset(), selection.getOffset() + selection.getLength(), null);
        editorAPI.setViewPort(editor, range.getStartLine(), range.getStartLine() + range.getNumberOfLines());

        //todo: here is original logic from eclipse. Adopt it for IntelliJ
       /* int lines = editor.getSelectionModel().getSelectionEndPosition().getLine()
                - editor.getSelectionModel().getSelectionStartPosition().getLine();
        int rangeTop = 0;
        int rangeBottom = 0;
        int selectionTop = 0;
        int selectionBottom = 0;

        if (selection != null)
        {
            try
            {
                selectionTop = editor.getSelectionModel().getSelectionStart();
                selectionBottom = editor.getSelectionModel().getSelectionEnd();
            }
            catch (Exception e)
            {
                // should never be reached
                LOG.error("Invalid line selection: offset: "
                        + selection.getOffset() + ", length: "
                        + selection.getLength());

                selection = null;
            }
        }

        if (range != null)
        {
            if (range.getStartLine() == -1)
            {
                range = null;
            }
            else
            {
                rangeTop = Math.min(lines - 1, range.getStartLine());
                rangeBottom = Math.min(lines - 1,
                        rangeTop + range.getNumberOfLines());
            }
        }

        if (range == null && selection == null)
        {
            return;
        }

        // top line of the new viewport
        int topPosition;
        int localLines = lines;
        int remoteLines = rangeBottom - rangeTop;
        int sizeDiff = remoteLines - localLines;

        // initializations finished

        if (range == null || selection == null)
        {
            topPosition = (rangeTop + rangeBottom + selectionTop + selectionBottom) / 2;
            editor.getScrollingModel() .setTopIndex(topPosition);

            return;
        }

        *//*
         * usually the viewport of the follower and the viewport of the followed
         * user will have the same center (this calculation). Exceptions may be
         * made below.
         *//*
        int center = (rangeTop + rangeBottom) / 2;
        topPosition = center - localLines / 2;

        if (sizeDiff <= 0)
        {
            // no further examination necessary when the local viewport is the
            // larger one
            viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
            return;
        }

        boolean selectionTopInvisible = (selectionTop < rangeTop + sizeDiff / 2);
        boolean selectionBottomInvisible = (selectionBottom > rangeBottom
                - sizeDiff / 2 - 1);

        if (rangeTop == 0
                && !(selectionTop <= rangeBottom && selectionTop > rangeBottom
                - sizeDiff))
        {
            // scrolled to the top and no selection at the bottom of range
            topPosition = 0;

        }
        else if (rangeBottom == lines - 1
                && !(selectionBottom >= rangeTop && selectionBottom < rangeTop
                + sizeDiff))
        {
            // scrolled to the bottom and no selection at the top of range
            topPosition = lines - localLines;

        }
        else if (selectionTopInvisible && selectionBottom >= rangeTop)
        {
            // making selection at top of range visible
            topPosition = Math.max(rangeTop, selectionTop);

        }
        else if (selectionBottomInvisible && selectionTop <= rangeBottom)
        {
            // making selection at bottom of range visible
            topPosition = Math.min(rangeBottom, selectionBottom) - localLines
                    + 1;
        }

        viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
   */
    }

    public void setEditable(SPath path, boolean editable) {
        Document doc = getDocument(path.getFile().getLocation().toFile());
        if (doc != null) {
            doc.setReadOnly(!editable);
        }
    }


    public SPath toPath(VirtualFile virtualFile) {
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

    public void reloadDocuments() {
        for (Document doc : getEditorPool().getDocuments()) {
            projectAPI.reloadFromDisk(doc);
        }
    }

    public boolean isOpenEditor(SPath path) {
        Document doc = editorPool.getDocument(path);
        if (doc == null) {
            return false;
        }

        return projectAPI.isOpen(doc);
    }

    public void lockAllEditors(boolean lock) {

        enableListeners(!lock);

        for (Document doc : editorPool.getDocuments()) {
            doc.setReadOnly(lock);
        }

    }

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

    public void registerNewFile(VirtualFile virtualFile, byte[] content) {
        this.newFiles.put(virtualFile, content);
    }

    public DocumentProvider getAdapter() {
        return adapter;
    }

    public EditorPool getEditorPool() {
        return editorPool;
    }


    public EditorAPI getEditorAPI() {
        return editorAPI;
    }

    public VirtualFile toVirtualFile(SPath path) {
        return toVirtualFile(path.getFile().getLocation().toFile());
    }

    public VirtualFile toVirtualFile(File path) {
        return localFileSystem.refreshAndFindFileByIoFile(path);
    }

    public Document getDocument(final File file) {

        return fileDocumentManager.getDocument(toVirtualFile(file));
    }
}
