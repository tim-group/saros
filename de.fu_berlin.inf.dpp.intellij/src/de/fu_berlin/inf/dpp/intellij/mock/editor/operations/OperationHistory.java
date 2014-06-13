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

package de.fu_berlin.inf.dpp.intellij.mock.editor.operations;

import de.fu_berlin.inf.dpp.intellij.mock.editor.exceptions.ExecutionException;
import de.fu_berlin.inf.dpp.intellij.mock.editor.operations.events.IOperationHistoryListener;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-16
 * Time: 09:14
 */

public class OperationHistory implements IOperationHistory
{
    @Override
    public void addOperationApprover(IOperationApprover operationBlocker)
    {
        System.out.println("OperationHistory.addOperationApprover //todo");
    }

    @Override
    public void removeOperationApprover(IOperationApprover operationBlocker)
    {
        System.out.println("OperationHistory.removeOperationApprover //todo");
    }

    @Override
    public void addOperationHistoryListener(IOperationHistoryListener historyListener)
    {
        System.out.println("OperationHistory.addOperationHistoryListener //todo");
    }

    @Override
    public void removeOperationHistoryListener(IOperationHistoryListener historyListener)
    {
        System.out.println("OperationHistory.removeOperationHistoryListener //todo");
    }

    @Override
    public void execute(IUndoableOperation auxOp, Object o1, Object o2) throws ExecutionException
    {
        System.out.println("OperationHistory.execute //todo");
    }

    @Override
    public void undo(IUndoContext context, Object o1, Object o2)
    {
        System.out.println("OperationHistory.undo //todo");
    }

    @Override
    public boolean canRedo(IUndoContext context)
    {
        return false;
    }
}
