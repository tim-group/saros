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

package de.fu_berlin.inf.dpp.intellij.editor.adapter;

import com.intellij.openapi.util.TextRange;

/**
 * Wrapper class for IntelliJ native document
 */
public class DocumentAdapter implements IDocument
{
    private com.intellij.openapi.editor.Document doc;

    public DocumentAdapter(com.intellij.openapi.editor.Document doc)
    {
        this.doc = doc;
    }



    @Override
    public String get(int offset, int length)
    {
        return doc.getText(new TextRange(offset,offset+length));
    }

    @Override
    public String get()
    {
       return doc.getText();
    }

    @Override
    public void replace(int offset, int length, String text)
    {
        doc.replaceString(offset,offset+length,text);
    }

    @Override
    public int getLength()
    {
        return doc.getTextLength();
    }

    @Override
    public int getNumberOfLines()
    {
        return doc.getLineCount();
    }

}
