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

package de.fu_berlin.inf.dpp.core.project;

/**
 * An interface that can be used to access checksums. So they do not need to be
 * recalculated as long as they are not invalid.
 *
 * @author Stefan Rossbach
 * @NOTE as we have currently no abstraction between the business logic and
 * Eclipse the unique identifier must match same layout as created by
 * {@link FileContentNotifierBridge} class !
 */
public interface IChecksumCache
{

    /**
     * Returns the checksum for the given unique identifier.
     *
     * @param path a unique identifier
     * @return the checksum or <code>null</code> if no checksum for this
     *         identifier exists or the checksum has become invalid
     */
    public abstract Long getChecksum(String path);

    /**
     * Adds or update a checksum in the cache.
     *
     * @param path     a unique identifier
     * @param checksum the checksum to add
     * @return <code>true</code> if the former checksum was invalid,
     *         <code>false</code> otherwise
     */
    public abstract boolean addChecksum(String path, long checksum);

}
