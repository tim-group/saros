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

package de.fu_berlin.inf.dpp.core.invitation.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SessionNegotiationHookManager
{
    private List<ISessionNegotiationHook> hooks;

    public SessionNegotiationHookManager()
    {
        hooks = new CopyOnWriteArrayList<ISessionNegotiationHook>();
    }

    public void addHook(ISessionNegotiationHook hook)
    {
        hooks.add(hook);
    }

    public void removeHook(ISessionNegotiationHook hook)
    {
        hooks.remove(hook);
    }

    public List<ISessionNegotiationHook> getHooks()
    {
        return new ArrayList<ISessionNegotiationHook>(hooks);
    }
}
