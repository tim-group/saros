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
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * This observable contains the ISarosSession that is currently open or null if
 * no session is open.
 * 
 * The observable value is set to a new session before ISarosSession.first() is
 * called and before the ISessionListeners are notified that the session has
 * started.
 * 
 * The observable value is set to null after ISarosSession.stop() is called but
 * before the ISessionListeners are notified that the session has ended.
 */
@Component(module = "observables")
public class SarosSessionObservable extends ObservableValue<ISarosSession> {

    public SarosSessionObservable() {
        super(null);
    }

}