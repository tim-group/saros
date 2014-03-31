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

package de.fu_berlin.inf.dpp.intellij.editor;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.editor.IEditorManager;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.1
 * Time: 10.07
 */

public class EditorManager implements IEditorManager
{
    @Override
    public void colorChanged()
    {
        System.out.println("EditorManager.colorChanged");
    }

    @Override
    public void refreshAnnotations()
    {
        System.out.println("EditorManager.refreshAnnotations");
    }

    @Override
    public void setAllLocalOpenedEditorsLocked(boolean locked)
    {
        System.out.println("EditorManager.setAllLocalOpenedEditorsLocked");
    }

    @Override
    public SPath[] getOpenEditorsOfAllParticipants()
    {
        System.out.println("EditorManager.getOpenEditorsOfAllParticipants");
        return new SPath[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveText(SPath path)
    {
        System.out.println("EditorManager.saveText");
    }
}
