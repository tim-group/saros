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
import de.fu_berlin.inf.dpp.activities.SPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-18
 * Time: 12:14
 */

public class EditorPool {
    private Map<SPath, Editor> editors = new HashMap<SPath, Editor>();
    private Map<SPath, Document> documents = new HashMap<SPath, Document>();
    private Map<Document, SPath> files = new HashMap<Document, SPath>();

    public EditorPool() {
    }

    public void add(SPath file, Editor editor) {
        editors.put(file, editor);
        add(file, editor.getDocument());
    }

    public void add(SPath file, Document document) {
        documents.put(file, document);
        files.put(document, file);
    }

    public void remove(SPath file) {
        if (editors.containsKey(file)) {
            editors.remove(file);
        }

        Document doc = null;
        if (documents.containsKey(file)) {
            doc = documents.remove(file);
        }

        if (doc != null) {
            files.remove(doc);
        }


    }

    public Collection<Document> getDocuments() {
        return documents.values();
    }

    public Document getDocument(SPath file) {
        return documents.get(file);
    }

    public Editor getEditor(SPath file) {
        return editors.get(file);
    }

    public SPath getFile(Document doc) {
        return files.get(doc);
    }

    public Set<SPath> getFiles() {
        return documents.keySet();

    }

    public void clear() {
        documents.clear();
        editors.clear();
        files.clear();
    }

}
