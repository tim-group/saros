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
 * This class represents the result of a version compatibility negotiation.
 */
public class VersionCompatibilityResult
{

    private final Compatibility compatibility;
    private Version localVersion;
    private Version remoteVersion;

    VersionCompatibilityResult(final Compatibility compatibility,
            final Version localVersion, final Version remoteVersion)
    {
        this.compatibility = compatibility;
        this.localVersion = localVersion;
        this.remoteVersion = remoteVersion;
    }

    /**
     * Returns the {@link Compatibility compatibility} of the negotiation
     * result.
     *
     * @return
     */
    public Compatibility getCompatibility()
    {
        return compatibility;
    }

    /**
     * Returns the local version that was used for during the negotiation.
     *
     * @return
     */
    public Version getLocalVersion()
    {
        return localVersion;
    }

    /**
     * Returns the remote version that was used for during the negotiation.
     *
     * @return
     */
    public Version getRemoteVersion()
    {
        return remoteVersion;
    }

}
