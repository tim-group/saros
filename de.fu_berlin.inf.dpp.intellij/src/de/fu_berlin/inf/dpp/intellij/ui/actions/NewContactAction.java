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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;

/**
 * Action to add contact. Not implemented yet.
 */
public class NewContactAction extends AbstractSarosAction {
    public static final String NAME = "addContact";

    @Override
    public String getActionName() {
        return NAME;
    }

    @Override
    public void execute() {
        actionStarted();

        LOG.info("Not implemented action [" + NAME + "]");

        SafeDialogUtils.showError(
            "We are sorry, but action [" + NAME + "] not implemented yet!",
            "Not Implemented");

        actionFinished();
    }
}
