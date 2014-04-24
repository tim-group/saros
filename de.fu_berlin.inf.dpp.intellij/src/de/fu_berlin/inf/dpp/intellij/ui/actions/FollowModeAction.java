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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.intellij.SarosToolWindowFactory;
import de.fu_berlin.inf.dpp.intellij.editor.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.ui.actions.core.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtil;
import org.jivesoftware.smack.RosterEntry;

import java.io.File;
import java.util.Collection;


/**
 * Follows active session
 * <p/>
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */
public class FollowModeAction extends AbstractSarosAction
{
    public static final String NAME = "follow";


    public static File file = new File("c:\\Develop\\Saros\\idea\\test_projects\\testas1\\src\\test1.java");

    @Override
    public String getActionName()
    {
        return NAME;

    }

    @Override
    public void run()
    {
        actionStarted();

        //todo
        log.info("FOLLOW - not implemented action");

        log.info("Listing roasted contacts....");
        final Collection<RosterEntry> entries = saros.getConnectionService().getRoster().getEntries();
        for (RosterEntry entry : entries)
        {
            log.info(entry);
        }



        EditorAPI api = new EditorAPI(SarosToolWindowFactory._project);
        VirtualFile vf = api.toVirtualFile(FollowModeAction.file);


        if(api.isOpen(vf))
        {
            System.out.println("FollowModeAction.run CLOSE "+vf);
            api.closeEditor(vf);
        }
         else
        {
            System.out.println("FollowModeAction.run OPEN "+vf);
           Editor edit = api.openEditor(vf);
            Document doc = api.createDocument(vf);
            //Editor edit = api.openEditor(doc);
            System.out.println("FollowModeAction.run>>>"+edit.getDocument().getLineNumber(1));


        }

        DialogUtil.showError("We are sorry, but action [" + NAME + "] not implemented yet!", "Not Implemented");

        actionFinished();
    }
}
