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

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * IntelliJ color manager
 */
//todo: temporary implementation to provide random colors
public class ColorManager
{

    private Map<Integer, ColorModel> colorMapDefault = new HashMap<Integer, ColorModel>();
    private Map<Integer, ColorModel> colorMap = new HashMap<Integer, ColorModel>();

    public ColorManager()
    {

        colorMapDefault.put(0, new ColorModel(Color.RED));
        colorMapDefault.put(1, new ColorModel(Color.BLUE));
        colorMapDefault.put(3, new ColorModel(Color.GREEN));
        colorMapDefault.put(4, new ColorModel(Color.cyan));
        colorMapDefault.put(5, new ColorModel(Color.MAGENTA));
        colorMapDefault.put(6, new ColorModel(Color.ORANGE));
        colorMapDefault.put(7, new ColorModel(Color.PINK));
        colorMapDefault.put(8, new ColorModel(Color.YELLOW));
    }

    public ColorModel getColorModel(int userId)
    {
        ColorModel color = colorMap.get(userId);

        if (color == null)
        {
            //get random id
            int id = (int) ((colorMapDefault.size()) * Math.random());
            color = colorMapDefault.get(id);
            if (color == null)
            {
                color = new ColorModel();
            }
            else
            {
                colorMapDefault.remove(id);
            }
            colorMap.put(userId, color);
        }
        return color;

    }

}
