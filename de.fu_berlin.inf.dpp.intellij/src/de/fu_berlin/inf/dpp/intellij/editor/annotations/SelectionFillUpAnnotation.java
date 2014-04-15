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

import de.fu_berlin.inf.dpp.session.User;

/**
 * This Annotation amends the {@link SelectionAnnotation}. It fills up
 * highlighted lines to the right margin, thus making them a proper selection
 * block. For details concerning the actual drawing see
 * {@link SelectionFillUpStrategy}.
 */
public class SelectionFillUpAnnotation extends SarosAnnotation {

    public static final String TYPE = "de.fu_berlin.inf.dpp.editor.annotations.SelectionFillUpAnnotation";

    private int length;
    private int offset;

    public SelectionFillUpAnnotation(User user, int offset, int length) {
        super(TYPE, true, "SelectionFillUpAnnotation", user);

        this.offset = offset;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    /**
     * @return <code>true</code> if drawing this Annotation should only clean
     *         the canvas and there should be no attempt to draw anything. This
     *         usually indicates that the user deselected some text, e.g. by
     *         clicking somewhere.
     */
    public boolean isDeselection() {
        return (length == 0);
    }

}
