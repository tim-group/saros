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

package de.fu_berlin.inf.dpp.intellij.editor.annotations;


import de.fu_berlin.inf.dpp.intellij.editor.mock.Canvas;
import de.fu_berlin.inf.dpp.intellij.editor.mock.Display;
import de.fu_berlin.inf.dpp.intellij.editor.mock.GC;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IAnnotationPresentation;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

import java.awt.*;


/**
 * The annotation that shows the viewports of users with
 * {@link Permission#WRITE_ACCESS}.
 * <p/>
 * Configuration of this annotation is done in the plugin-xml.
 *
 * @author rdjemili
 */
public class ViewportAnnotation extends SarosAnnotation implements
        IAnnotationPresentation
{

    protected static final String TYPE = "de.fu_berlin.inf.dpp.annotations.viewport";

    private static final int INSET = 2;

    private static final float STROKE_SCALE = 1.05f;

    private static final float FILL_SCALE = 1.22f;

    private Color strokeColor;

    private Color fillColor;

    private boolean multipleLines = false;

    public ViewportAnnotation(User source)
    {
        super(ViewportAnnotation.TYPE, true, "Visible scope of "
                + source.getHumanReadableName(), source);

        Display display = Display.getDefault();

        Color currentColor = getColor(TYPE, source.getColorID());

        int rgb = currentColor.getRGB();

        //currentColor.dispose(); //todo

        // strokeColor = new Color(display, ColorUtils.scaleColorBy(rgb,STROKE_SCALE));
        strokeColor = new Color(rgb + 20);  //todo
        // FIXME: dispose strokeColor somewhere
        // fillColor = new Color(display, ColorUtils.scaleColorBy(rgb, FILL_SCALE));
        fillColor = new Color(rgb - 20); //todo
        // FIXME: dispose fillColor somewhere
    }

    @Override
    public void paint(GC gc, Canvas canvas, Rectangle bounds)
    {
        Dimension canvasSize = canvas.getSize();

        gc.setBackground(fillColor);
        gc.setForeground(strokeColor);
        gc.setLineWidth(1);

        int x = ViewportAnnotation.INSET;
        int y = bounds.y;
        int w = (int) canvasSize.getWidth() - 2 * ViewportAnnotation.INSET;
        int h = bounds.height;

        if (multipleLines)
        {
            h += gc.getFontMetrics().getHeight();
        }

        if (y < 0)
        {
            h = h + y;
            y = 0;
        }

        if (h <= 0)
        {
            return;
        }

        gc.fillRectangle(x, y, w, h);
        gc.drawRectangle(x, y, w, h);
    }

    /**
     * Enables the advanced computation of the Viewport, because the calculation
     * of the viewport annotation differs between files with one line and files
     * with more than one line.
     *
     * @param multipleLines boolean flag that signs, if the editor has more than one line
     */
    public void setMoreThanOneLine(boolean multipleLines)
    {
        this.multipleLines = multipleLines;
    }

    @Override
    public int getLayer()
    {
        return IAnnotationPresentation.DEFAULT_LAYER;
    }
}
