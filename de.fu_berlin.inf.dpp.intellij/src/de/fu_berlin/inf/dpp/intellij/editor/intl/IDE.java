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

package de.fu_berlin.inf.dpp.intellij.editor.intl;

import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.intl.exceptions.PartInitException;
import de.fu_berlin.inf.dpp.intellij.editor.intl.ui.IEditorDescriptor;
import de.fu_berlin.inf.dpp.intellij.editor.intl.ui.IWorkbenchPage;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-15
 * Time: 15:43
 */

public class IDE
{
    public static IEditorDescriptor getEditorDescriptor(IFile fiel)
    {
        //todo
        System.out.println("IDE.getEditorDescriptor //todo");
         return null;
    }

    public static IEditorPart openEditor(IWorkbenchPage page,IFile file, boolean activate)  throws PartInitException
    {
        //todo
        System.out.println("IDE.openEditor //todo");
           return null;
    }

    public static Boolean saveAllEditors(IResource[] resources, boolean confirm)
    {
        //todo
        System.out.println("IDE.saveAllEditors //todo");

        return null;
    }
}
