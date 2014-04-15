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


import de.fu_berlin.inf.dpp.intellij.editor.intl.text.events.DocumentEvent;
import de.fu_berlin.inf.dpp.intellij.editor.intl.text.IDocumentListener;

/**
 * A document listener which informs the given EditorManager of changes before
 * they occur in a document (using documentAboutToBeChanged). </p> This listener
 * can be temporarily disabled which prevents the notification of text change
 * events.
 *
 */
public class StoppableDocumentListener implements IDocumentListener
{

    private final EditorManager editorManager;

    private boolean enabled = true;

    StoppableDocumentListener(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    @Override
    public void documentAboutToBeChanged(final DocumentEvent event) {

        if (!enabled)
            return;

        String text = event.getText() == null ? "" : event.getText();

        editorManager.textAboutToBeChanged(event.getOffset(), text,
                event.getLength(), event.getDocument());

    }

    @Override
    public void documentChanged(final DocumentEvent event) {
        // do nothing. We handled everything in documentAboutToBeChanged
    }

    /**
     * Enables or disables the forwarding of text changes. Default is enabled.
     *
     * @param enabled
     *            <code>true</code> to forward text changes, <code>false</code>
     *            otherwise
     *
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}