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

package de.fu_berlin.inf.dpp.core.editor;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.intellij.editor.mock.eclipse.ISharedEditorListener;
import de.fu_berlin.inf.dpp.intellij.editor.mock.eclipse.RemoteEditorManager;
import de.fu_berlin.inf.dpp.session.IActivityProducerAndConsumer;

import java.io.FileNotFoundException;
import java.util.Set;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.27
 * Time: 12.32
 */

public interface IEditorManager extends IEditorManagerBase, IActivityProducerAndConsumer
{


    void generateSelection(IEditorPart part, ITextSelection newSelection);

    void generateViewport(IEditorPart part, ILineRange viewport);

    void partActivated(IEditorPart editorPart);

    void partOpened(IEditorPart editorPart);

    void partClosed(IEditorPart editor);

    void partInputChanged(IEditorPart editor);

    RemoteEditorManager getRemoteEditorManager();

    boolean isActiveEditorShared();

    void addSharedEditorListener(ISharedEditorListener listener);

    void removeSharedEditorListener(ISharedEditorListener listener);


    void saveLazy(SPath path) throws FileNotFoundException;

    Set<SPath> getLocallyOpenEditors();
    Set<SPath> getRemoteOpenEditors();
}
