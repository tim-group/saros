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

package de.fu_berlin.inf.dpp.core.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.ObservableValue;

/**
 * Observable containing the ID of the SarosSession in which the local user is
 * currently participating or is currently joining (during an invitation).
 *
 * If not in an invitation or shared project session the value of this
 * Observable equals {@link SessionIDObservable#NOT_IN_SESSION}.
 *
 * If in a shared project session the value of this Observable is the string
 * representation of a random integer.
 *
 * @deprecated The common usage in Saros is to use this observable as a global
 *             variable to perform global state programming which was not
 *             intended. Use {@link ISarosSession#getID()} instead which will be
 *             almost equivalent in most cases.
 */
@Deprecated
@Component(module = "observables")
public class SessionIDObservable extends ObservableValue<String> {

    public final static String NOT_IN_SESSION = "NOT_IN_SESSION";

    public SessionIDObservable() {
        super(NOT_IN_SESSION);
    }

    public boolean isInASession() {
        return NOT_IN_SESSION.equals(getValue());
    }
}
