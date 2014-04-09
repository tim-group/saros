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

package de.fu_berlin.inf.dpp.core.versioning;


/**
 * Enumeration to describe whether a local version is compatible with a remote
 * one.
 */
public enum Compatibility
{

    /**
     * Versions are (probably) compatible
     */
    OK(0)
            {
                @Override
                public Compatibility invert()
                {
                    return OK;
                }
            },
    /**
     * The local version is (probably) too old to work with the remote version.
     * <p/>
     * The user should be told to upgrade
     */
    TOO_OLD(1)
            {
                @Override
                public Compatibility invert()
                {
                    return TOO_NEW;
                }
            },
    /**
     * The local version is (probably) too new to work with the remote version.
     * <p/>
     * The user should be told to tell the peer to update.
     */
    TOO_NEW(2)
            {
                @Override
                public Compatibility invert()
                {
                    return TOO_OLD;
                }
            },

    /**
     * The compatibility could not be determined.
     */
    UNKNOWN(3)
            {
                @Override
                public Compatibility invert()
                {
                    return UNKNOWN;
                }
            };

    private final int code;

    Compatibility(final int code)
    {
        this.code = code;
    }

    /**
     * @return <code>TOO_OLD</code> if the initial compatibility was
     *         <code>TOO_NEW</code>, <code>TOO_NEW</code> if the initial
     *         compatibility was <code>TOO_OLD</code>, <code>OK</code> otherwise
     */
    public abstract Compatibility invert();

    public int getCode()
    {
        return code;
    }

    public static Compatibility fromCode(int code)
    {

        for (Compatibility compatibility : Compatibility.values())
        {
            if (compatibility.getCode() == code)
            {
                return compatibility;
            }
        }

        return UNKNOWN;
    }

    /**
     * Given a result from {@link java.util.Comparator#compare(Object, Object)} will
     * return the associated Compatibility object
     */
    public static Compatibility valueOf(int comparison)
    {
        switch (Integer.signum(comparison))
        {
            case -1:
                return TOO_OLD;
            case 0:
                return OK;
            case 1:
            default:
                return TOO_NEW;
        }
    }
}