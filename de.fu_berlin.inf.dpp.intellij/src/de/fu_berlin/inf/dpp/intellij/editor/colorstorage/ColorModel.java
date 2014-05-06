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

package de.fu_berlin.inf.dpp.intellij.editor.colorstorage;

import com.intellij.openapi.editor.markup.RangeHighlighter;

import java.awt.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-05
 * Time: 13:12
 */

public class ColorModel
{
    public static final int delta = 50;
    public static final int max = 255;
    public static final int max_auto = 200;

    private Color selectColor;
    private Color editColor;
    private RangeHighlighter select;

    public ColorModel(Color editColor, Color selectColor)
    {
        this.selectColor = selectColor;
        this.editColor = editColor;
    }

    public ColorModel(Color editColor)
    {
        this.editColor = editColor;
        int red = editColor.getRed() + delta;
        red = red > max ? max : red;

        int green = editColor.getGreen() + delta;
        green = green > max ? max : green;

        int blue = editColor.getBlue() + delta;
        blue = blue > max ? max : blue;

        this.selectColor = new Color(red, green, blue);
    }

    public ColorModel()
    {
        this(new Color(((int) (max_auto * Math.random())), ((int) (max_auto * Math.random())), ((int) (max_auto * Math.random()))));
    }


    public Color getSelectColor()
    {
        return selectColor;
    }

    public Color getEditColor()
    {
        return editColor;
    }

    public RangeHighlighter getSelect()
    {
        return select;
    }

    public void setSelect(RangeHighlighter select)
    {
        this.select = select;
    }
}
