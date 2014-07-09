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

package de.fu_berlin.inf.dpp.core.concurrent;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import de.fu_berlin.inf.dpp.activities.SPath;


/**
 * This Class represents a checksum of a document. It contains the path, the
 * length and the hash code of the document.
 *
 * @author chjacob
 */
public class DocumentChecksum {

    /**
     * Constant used for representing a missing file
     */
    public static final int NON_EXISTING_DOC = -1;

    protected DocumentListener dirtyListener = new DocumentListener() {

        @Override
        public void beforeDocumentChange(DocumentEvent documentEvent) {

        }

        @Override
        public void documentChanged(DocumentEvent event) {
            dirty = true;
        }
    };

    // the path to the concurrent document
    protected final SPath path;

    // the length of the document
    protected int length;

    // the hash code of the document
    protected int hash;

    protected Document document;

    protected boolean dirty;

    /**
     * Creates a new Checksum for the document represented in the given path.
     * <p/>
     * The checksum is initially created without being bound to a document.
     */
    public DocumentChecksum(SPath path) {
        this.path = path;
        this.dirty = true;
    }

    public SPath getPath() {
        return path;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public void setDirty(boolean b) {
        this.dirty = b;
    }

    public void dispose() {
        unbind();
    }

    private void unbind() {
        if (document != null) {
            document.removeDocumentListener(dirtyListener);
        }
    }

    public void bind(Document doc) {

        if (this.document == doc)
            return;

        unbind();

        this.document = doc;

        if (document != null)
            doc.addDocumentListener(dirtyListener);

        dirty = true;
    }

    public void update() {

        // If document not changed, skip
        if (!dirty)
            return;

        if (document == null) {
            this.length = this.hash = NON_EXISTING_DOC;
        } else {
            this.length = document.getTextLength();
            this.hash = document.getText().hashCode();
        }

        dirty = false;
    }

    /**
     * Returns whether this checksum represents a file which exists at the host.
     * <p/>
     * If false is returned, then this checksum indicates that the host has no
     * file under the given path.
     */
    public boolean existsFile() {
        return !(this.length == NON_EXISTING_DOC && this.hash == NON_EXISTING_DOC);
    }

    @Override
    public String toString() {
        return path.toString() + " [" + this.length + "," + this.hash + "]";
    }
}