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

package de.fu_berlin.inf.dpp.core.editor.colorstorage;

import java.io.Serializable;

/**
 * Holder class for a current and favorite color id.
 */
public final class UserColorID implements Serializable
{

    /**
     * A constant for presenting an unknown and invalid color id
     */
    public static final int UNKNOWN = -1;

    private static final long serialVersionUID = 1L;

    private int current;
    private int favorite;

    /**
     * Constructs a newly allocated UserColorID object with current and favorite
     * color id set to {@link #UNKNOWN}.
     */
    public UserColorID()
    {
        current = UNKNOWN;
        favorite = UNKNOWN;
    }

    /**
     * Constructs a newly allocated UserColorID object with current and favorite
     * color id set to the given values.
     *
     * @param current  the value to use for the current color id
     * @param favorite the value to use for the favorite color id
     */
    UserColorID(int current, int favorite)
    {
        this.current = current;
        this.favorite = favorite;
    }

    /**
     * Returns the current color id.
     *
     * @return the current color id
     */
    public int getCurrent()
    {
        return current;
    }

    /**
     * Returns the favorite color id.
     *
     * @return the favorite color id
     */
    public int getFavorite()
    {
        return favorite;
    }

    /**
     * Sets the current color id
     *
     * @param colorID the new color id to set
     */
    public void setCurrent(int colorID)
    {
        current = colorID;
    }

    /**
     * Sets the favorite color id
     *
     * @param colorID the new favorite color id to set
     */
    public void setFavorite(int colorID)
    {
        favorite = colorID;
    }

    /**
     * Checks if the given color id is valid. A negative color id will never be
     * valid.
     *
     * @param colorID the color id to check
     * @return <code>true</code> if the color id is valid, <code>false</code>
     *         otherwise
     */
    public static boolean isValid(int colorID)
    {
        return colorID >= 0;
    }
}

