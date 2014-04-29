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


import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;


/**
 * A document listener which informs the given EditorManagerEcl of changes before
 * they occur in a document (using documentAboutToBeChanged). </p> This listener
 * can be temporarily disabled which prevents the notification of text change
 * events.
 */
public class StoppableDocumentListener extends AbstractStoppableListener implements DocumentListener
{

    private final EditorManager editorManager;
    private Document document;

    public StoppableDocumentListener(EditorManager editorManager)
    {
        this.editorManager = editorManager;
    }

    @Override
    public void beforeDocumentChange(DocumentEvent event)
    {
        if (!enabled)
        {
            return;
        }

        editorManager.textAboutToBeChanged(event);
    }

    @Override
    public void documentChanged(DocumentEvent documentEvent)
    {
        // do nothing. We handled everything in documentAboutToBeChanged
    }

    public EditorManager getEditorManager()
    {
        return editorManager;
    }

    public Document getDocument()
    {
        return document;
    }

    public void setDocument(Document document)
    {
        if(document !=null)
        {
            if (this.document == null)
            {
                this.document = document;
                this.document.addDocumentListener(this);
            }
            else if (this.document != document)
            {
                this.document.removeDocumentListener(this);
                this.document = document;

                this.document.addDocumentListener(this);
            }
            else
            {
                //do nothing, as we listen that document
            }
        }
        else
        {
            if(this.document != null)
            {
                this.document.removeDocumentListener(this);
                this.document = null;
            }

        }

    }
}