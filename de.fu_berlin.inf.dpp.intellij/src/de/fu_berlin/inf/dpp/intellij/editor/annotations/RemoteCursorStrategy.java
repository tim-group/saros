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


import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IDrawingStrategy;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.Annotation;
import de.fu_berlin.inf.dpp.intellij.editor.mock.GC;
import de.fu_berlin.inf.dpp.intellij.editor.mock.StyledText;

import java.awt.*;

/**
 * Used to draw a cursor-like line at the position of the text cursor of a
 * remote user. This enables the local user to see exactly where the other
 * users' cursors are.
 */
public class RemoteCursorStrategy implements IDrawingStrategy
{
    private static final int CURSOR_WIDTH = 2;

    /**
     * {@inheritDoc}
     *
     * @param annotation
     *            An RemoteCursorAnnotation passed by the
     *            {@link AnnotationPainter}
     * @param offset
     *            offset of the end of the Selection
     * @param length
     *            always 0, will be ignored
     */
    @Override
    public void draw(Annotation annotation, GC gc, StyledText textWidget,
            int offset, int length, Color color) {
        Point currentCursorPosition = textWidget.getLocationAtOffset(offset);

        // clearing mode
        if (gc == null) {
            /*
             * Redraw the surrounding area of the cursor. Because we draw a line
             * with a width larger than 1, we have to clear the area around the
             * actual coordinates (start a bit more left, and extend a bit to
             * the right).
             */
            textWidget.redraw(currentCursorPosition.x - CURSOR_WIDTH / 2,
                    currentCursorPosition.y, CURSOR_WIDTH + 1,
                    textWidget.getLineHeight(), false);

            return;
        }

        final Color oldBackground = gc.getBackground();
        final Color oldForeground = gc.getForeground();

        /*
         * Draw the cursor line
         */
        gc.setBackground(color);
        gc.setForeground(color);

        gc.setLineWidth(CURSOR_WIDTH);
        gc.drawLine(currentCursorPosition.x, currentCursorPosition.y,
                currentCursorPosition.x,
                currentCursorPosition.y + textWidget.getLineHeight());

        // set back the colors like they were before
        gc.setBackground(oldBackground);
        gc.setForeground(oldForeground);
    }
}
