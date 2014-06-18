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

package de.fu_berlin.inf.dpp.intellij.ui.eventhandler;


import de.fu_berlin.inf.dpp.core.concurrent.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SarosUI;
import de.fu_berlin.inf.dpp.observables.ValueChangeListener;


/**
 * This handler is responsible for opening the SessionView if an inconsistency
 * has been detected.
 *
 */
public class SessionViewOpener {

    public SessionViewOpener(IsInconsistentObservable isInconsistentObservable,
            final SarosUI sarosUI) {
        isInconsistentObservable.add(new ValueChangeListener<Boolean>() {
            @Override
            public void setValue(Boolean inconsistency) {
                if (!inconsistency) {
                    return;
                }

                SWTUtils.runSafeSWTAsync(null, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        sarosUI.openSarosView();
                    }
                });
            }
        });
    }
}
