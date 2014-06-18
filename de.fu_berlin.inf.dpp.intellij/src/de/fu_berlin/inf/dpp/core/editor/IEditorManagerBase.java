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
import de.fu_berlin.inf.dpp.session.IActivityListener;

import java.io.FileNotFoundException;
import java.util.Set;


public interface IEditorManagerBase {
    void saveText(SPath path);

    Set<SPath> getOpenEditorsOfAllParticipants();

    void setAllLocalOpenedEditorsLocked(boolean locked);

    void colorChanged();

    void refreshAnnotations();

    boolean isOpenEditor(SPath path);

    void saveLazy(SPath path) throws FileNotFoundException;

    void closeEditor(SPath path);

    void openEditor(SPath path);

    void addActivityListener(IActivityListener listener);

    void removeActivityListener(IActivityListener listener);

    Set<SPath> getLocallyOpenEditors();

    Set<SPath> getRemoteOpenEditors();

    RemoteEditorManager getRemoteEditorManager();

    boolean isActiveEditorShared();

    void addSharedEditorListener(ISharedEditorListener listener);

    void removeSharedEditorListener(ISharedEditorListener listener);


}
