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


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.intellij.SarosToolWindowFactory;
import de.fu_berlin.inf.dpp.intellij.editor.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtil;

/**
 * Adds new contact
 * <p/>
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class NewContactAction extends AbstractSarosAction
{
    public static final String NAME = "addContact";

    @Override
    public String getActionName()
    {
        return NAME;
    }

    @Override
    public void run()
    {
        actionStarted();

        final int rnd1 = ((int) (50 * Math.random()));
        int rnd2 = ((int) (10000 * Math.random()));
        final String text = " insert_" + rnd2 + " ";

        EditorAPI api = new EditorAPI(SarosToolWindowFactory._project);
        VirtualFile vf = api.toVirtualFile(FollowModeAction.file);
        Editor edit = api.openEditor(vf);
        api.insertText(edit.getDocument(),rnd1,text);
        api.setSelection(edit,rnd1, rnd1 + text.length());

        //todo
        log.info("ADD_CONTACT - not implemented action");


        DialogUtil.showError("We are sorry, but action [" + NAME + "] not implemented yet!", "Not Implemented");

        actionFinished();
    }
}
