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

package de.fu_berlin.inf.dpp.intellij.editor.intl.text;

import de.fu_berlin.inf.dpp.intellij.editor.intl.exceptions.BadLocationException;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-16
 * Time: 11:13
 */

public class Document implements IDocument
{
    @Override
    public void addDocumentListener(IDocumentListener listener)
    {
        System.out.println("Document.addDocumentListener //todo");
    }

    @Override
    public void removeDocumentListener(IDocumentListener listener)
    {
        System.out.println("Document.removeDocumentListener //todo");
    }

    private String text = "This Swing Java Tutorial describes developing graphical user interfaces (GUIs) for applications and applets using Swing components.";

    @Override
    public String get(int offset, int length) throws BadLocationException
    {
        System.out.println("Document.get //todo O="+offset+" L="+length);
        return text.substring(offset,offset+length);
    }

    @Override
    public String get()
    {
        System.out.println("Document.get //todo");
        return text;
    }

    @Override
    public void replace(int offset, int length, String text) throws BadLocationException
    {
        System.out.println("Document.replace");
    }

    @Override
    public int getLength()
    {
        return 0;
    }

    @Override
    public int getNumberOfLines()
    {
        return 0;
    }

    @Override
    public int getLineOfOffset(int offset) throws BadLocationException
    {
        return 0;
    }

    @Override
    public int getLineOffset(int top)
    {
        return 0;
    }
}
