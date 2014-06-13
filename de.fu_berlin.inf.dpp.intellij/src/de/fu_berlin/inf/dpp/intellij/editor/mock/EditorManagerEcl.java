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

package de.fu_berlin.inf.dpp.intellij.editor.mock;

import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.DocumentProvider;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.DocumentProviderRegistry;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IDocumentProvider;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IEditorInput;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-06-13
 * Time: 09:29
 */

public class EditorManagerEcl {

    /**
     * Returns the {@link IDocumentProvider} of the given {@link IEditorInput}.
     * This method analyzes the file extension of the {@link IFile} associated
     * with the given {@link IEditorInput}. Depending on the file extension it
     * returns file-types responsible {@link IDocumentProvider}.
     *
     * @param input the {@link IEditorInput} for which {@link IDocumentProvider}
     *              is needed
     * @return IDocumentProvider of the given input
     */
    public static IDocumentProvider getDocumentProvider(IEditorInput input)
    {
        return DocumentProviderRegistry.getDefault().getDocumentProvider(input);
    }
}
