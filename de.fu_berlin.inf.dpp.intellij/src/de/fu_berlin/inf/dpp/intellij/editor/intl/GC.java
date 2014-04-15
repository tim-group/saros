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

package de.fu_berlin.inf.dpp.intellij.editor.intl;

import java.awt.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-15
 * Time: 11:56
 */

public class GC
{
    Color background;
    Color foreground;
    int lineWidth;

    public Color getBackground()
    {
        return background;
    }

    public void setBackground(Color background)
    {
        this.background = background;
    }

    public Color getForeground()
    {
        return foreground;
    }

    public void setForeground(Color foreground)
    {
        this.foreground = foreground;
    }

   public void setLineWidth(int cursorWidth)
    {
        this.lineWidth = cursorWidth;
    }

    public void drawLine(int x, int y, int toX, int toY)
    {
        System.out.println("GC.drawLine");
    }

    public void fillRectangle(int x,int y, int w,int h)
    {
        //todo
        System.out.println("GC.fillRectangle");
    }

    public void drawRectangle(int x,int y, int w,int h)
    {
        //todo
        System.out.println("GC.drawRectangle");
    }

    public Rectangle getFontMetrics()
    {
        //todo
        System.out.println("GC.getFontMetrics");
        return null;
    }
}
