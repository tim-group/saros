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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-18
 * Time: 12:14
 */

public class EditorPool
{
    private Map<VirtualFile, Editor> editors = new HashMap<VirtualFile, Editor>();
    private Map<VirtualFile, Document> documents = new HashMap<VirtualFile, Document>();

    public EditorPool()
    {
    }

    public void add(VirtualFile file, Editor editor)
    {
        editors.put(file, editor);
        documents.put(file, editor.getDocument());
    }

    public void add(VirtualFile file, Document document)
    {
        documents.put(file, document);
    }

    public void remove(VirtualFile file)
    {
        if (editors.containsKey(file))
        {
            editors.remove(file);
        }

        if(documents.containsKey(file))
        {
           documents.remove(file);
        }
    }

    public Document getDocument(VirtualFile file)
    {
        return documents.get(file);
    }

    public Editor getEditor(VirtualFile file)
    {
        return editors.get(file);
    }

    public void clear()
    {
        documents.clear();
        editors.clear();
    }

}
