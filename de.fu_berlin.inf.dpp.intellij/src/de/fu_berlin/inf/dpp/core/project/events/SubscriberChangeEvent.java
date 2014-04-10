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

package de.fu_berlin.inf.dpp.core.project.events;

import de.fu_berlin.inf.dpp.core.project.ISubscriber;
import de.fu_berlin.inf.dpp.filesystem.IResource;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-09
 * Time: 17:24
 */

public interface SubscriberChangeEvent
{
    // Field descriptor #2 I
    public static final int NO_CHANGE = 0;

    // Field descriptor #2 I
    public static final int SYNC_CHANGED = 1;

    // Field descriptor #2 I
    public static final int ROOT_ADDED = 2;

    // Field descriptor #2 I
    public static final int ROOT_REMOVED = 4;


    // Method descriptor #18 ()Lorg/eclipse/core/resources/IResource;
    public IResource getResource();

    // Method descriptor #19 ()Lorg/eclipse/team/core/subscribers/Subscriber;
    ISubscriber getSubscriber();


   int getFlags();
}
