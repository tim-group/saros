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

package de.fu_berlin.inf.dpp.intellij.editor.mock.events;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-15
 * Time: 09:35
 */

public class PropertyChangeEvent
{
    private IPreferenceStore store;
    private String name;
    private Object oldValue;
    private Object newValue;

    public PropertyChangeEvent(IPreferenceStore store, String name, Object oldValue, Object newValue)
    {
        this.store = store;
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getProperty()
    {
        //todo
        System.out.println("PropertyChangeEvent.getProperty //todo");
        return null;
    }

    public PropertyChangeEvent getNewValue()
    {
        //todo
        System.out.println("PropertyChangeEvent.getNewValu //todoe");
        return null;
    }
}
