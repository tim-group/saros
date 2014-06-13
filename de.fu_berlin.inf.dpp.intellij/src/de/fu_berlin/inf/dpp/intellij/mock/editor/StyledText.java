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

package de.fu_berlin.inf.dpp.intellij.mock.editor;

import de.fu_berlin.inf.dpp.intellij.mock.editor.events.ControlListener;
import de.fu_berlin.inf.dpp.intellij.mock.editor.events.MouseListener;

import java.awt.*;
import java.awt.event.KeyListener;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-15
 * Time: 11:55
 */

public class StyledText
{
    public Point getLocationAtOffset(int i)
    {
        //todo
        System.out.println("StyledText.getLocationAtOffset //todo");
        return null;
    }

    public int getLineHeight()
    {
        //todo
        return 0;
    }

    public void redraw(int x, int y, int height,int cursorWidth, boolean b)
    {
        //todo
        System.out.println("StyledText.redraw //todo");
    }

    public int getData(String foldingOffsetKey)
    {
        //todo
        System.out.println("StyledText.getData");

        return 0;
    }

    public void setData(String foldingOffsetKey, Integer offset)
    {
        System.out.println("StyledText.setData");
    }

    public int getLineAtOffset(int offset)
    {
        System.out.println("StyledText.getLineAtOffset");
        return 0;

    }

    public int getOffsetAtLine(int lineNo)
    {
        System.out.println("StyledText.getOffsetAtLine");
        return 0;
    }

    public String getLine(int lineNo)
    {
        return null;
    }

    public Point getSelection()
    {
        System.out.println("StyledText.getSelection");
        return null;
    }

    public int getCharCount()
    {
        return 0;
    }

    public Rectangle getBounds()
    {
        return null;
    }

    public Rectangle getTextBounds(int a, int b)
    {
        return null;
    }

    public void removeControlListener(ControlListener listener)
    {
        System.out.println("StyledText.removeControlListener");
    }

    public void addControlListener(ControlListener listener)
    {
        System.out.println("StyledText.addControlListener");
    }

    public void removeMouseListener(MouseListener listener)
    {
        System.out.println("StyledText.removeMouseListener");
    }

    public void addMouseListener(MouseListener listener)
    {
        System.out.println("StyledText.addMouseListener");
    }

    public void removeKeyListener (KeyListener listener)
    {
        System.out.println("StyledText.removeKeyListener");
    }

    public void addKeyListener (KeyListener listener)
    {
        System.out.println("StyledText.addKeyListener");
    }
}
