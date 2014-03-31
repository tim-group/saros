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

/**
 * This observable can be used to check whether there is currently a file
 * replacement activityDataObject in progress by the ConsistencyWatchdog (in
 * this case isReplacementInProgress() returns true).
 *
 * Internally this class uses reference counting, so you can call
 * startReplacement() repeatedly and it will return true until a matching number
 * of calls to replacementDone() has been made.
 */
@Component(module = "observables")
public class FileReplacementInProgressObservable {

    int numberOfFileReplacementsInProgress = 0;

    public synchronized boolean isReplacementInProgress() {
        return numberOfFileReplacementsInProgress > 0;
    }

    public synchronized void startReplacement() {
        numberOfFileReplacementsInProgress++;
    }

    public synchronized void replacementDone() {
        numberOfFileReplacementsInProgress--;
    }
}
