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

package de.fu_berlin.inf.dpp.core.exceptions;

import de.fu_berlin.inf.dpp.core.invitation.ProcessTools;

/**
 * Exception used for signaling that the local user canceled an operation
 */
public class LocalCancellationException extends SarosCancellationException
{

    private static final long serialVersionUID = 3663315740957551184L;
    protected ProcessTools.CancelOption cancelOption;

    /**
     * Standard constructor.
     * <p/>
     * If no {@link CancelOption} is specified {@link CancelOption#NOTIFY_PEER}
     * is set.
     */
    public LocalCancellationException()
    {
        super();
        this.cancelOption = ProcessTools.CancelOption.NOTIFY_PEER;
    }

    public LocalCancellationException(String msg,
            ProcessTools.CancelOption cancelOption)
    {
        super(msg);
        this.cancelOption = cancelOption;
    }

    public ProcessTools.CancelOption getCancelOption()
    {
        return cancelOption;
    }
}
