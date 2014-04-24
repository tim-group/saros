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
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import org.apache.log4j.Logger;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-18
 * Time: 15:49
 */

public class EditorActionManager
{
    private Logger log = Logger.getLogger(EditorActionManager.class);
    private EditorPool editorPool;
    private EditorAPI editorAPI;

    public EditorActionManager()
    {
        this.editorPool = new EditorPool();
        this.editorAPI = new EditorAPI();
    }

    public EditorActionManager(EditorPool editorPool)
    {
        this.editorPool = editorPool;
        this.editorAPI = new EditorAPI();
    }

    public void openFile(SPath file)
    {
        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        if (virtualFile.exists())
        {
            Editor editor = editorAPI.openEditor(virtualFile);
            editorPool.add(virtualFile, editor);
        }
        else
        {
            log.warn("File not exist " + file);
        }
    }

    public void closeFile(SPath file)
    {
        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        if (virtualFile.exists())
        {
            editorAPI.closeEditor(virtualFile);
            //editorPool.remove(virtualFile);
        }
        else
        {
            log.warn("File not exist " + file);
        }
    }

    public void saveFile(SPath file)
    {
        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        Document doc = editorPool.getDocument(virtualFile);
        if (doc != null)
        {
            editorAPI.saveDocument(doc);
        }
        else
        {
            log.warn("Document not exist " + file);
        }
    }

    public void editText(SPath file, Operation operations)
    {
        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        Document doc = editorPool.getDocument(virtualFile);

        for (ITextOperation op : operations.getTextOperations())
        {
            if (op instanceof DeleteOperation)
            {
                editorAPI.deleteText(doc, op.getPosition(), op.getPosition() + op.getTextLength());
            }
            else
            {
                editorAPI.insertText(doc, op.getPosition(), op.getText());
            }
        }


    }

    public void selectText(SPath file, int position, int length)
    {
        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        Editor editor = editorPool.getEditor(virtualFile);
        if (editor != null)
        {
            editorAPI.setSelection(editor, position, position + length); //todo: calculate new line char win and unix differences

        }
    }

    public void setViewPort(final SPath file, final int lineStart, final int lineEnd)
    {
        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        Editor editor = editorPool.getEditor(virtualFile);
        if (editor != null)
        {
            editorAPI.setViewPort(editor, lineStart, lineEnd);
        }
    }

    public EditorPool getEditorPool()
    {
        return editorPool;
    }
}
