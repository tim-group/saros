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
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.session.AbstractActivityProvider;
import org.apache.log4j.Logger;

/**
 * //todo: temporary class to be removed after full migration
 *
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-29
 * Time: 09:00
 */

public abstract class EditorManagerBridge extends AbstractActivityProvider implements IEditorManager
{

    protected static final Logger log = Logger.getLogger(EditorManagerBridge.class.getName());


    @Override
    public void generateSelection(IEditorPart part, ITextSelection newSelection)
    {
        System.out.println("EditorManager.generateSelection //todo P="+part+" S="+newSelection);
    }

    @Override
    public void generateViewport(IEditorPart part, ILineRange viewport)
    {
        System.out.println("EditorManager.generateViewport E="+part+" WP="+viewport);
    }



    /**
     * Called when the local user activated a shared editor.
     * <p/>
     * This can be called twice for a single IEditorPart, because it is called
     * from partActivated and from partBroughtToTop.
     * <p/>
     * We do not filter duplicate events, because it would be bad to miss events
     * and is not too bad have duplicate one's. In particular we use IPath as an
     * identifier to the IEditorPart which might not work for multiple editors
     * based on the same file.
     */
    public void partActivated(IEditorPart editorPart)
    {

        System.out.println("EditorManagerBridge.partActivated E="+editorPart);

        // First check for last editor being closed (which is a null editorPart)
        if (editorPart == null)
        {
            // generateEditorActivated(null);
            return;
        }

      /* // Is the new editor part supported by Saros (and inside the project)
        // and the Resource accessible (we don't want to report stale files)?
        IResource resource = editorAPI.getEditorResource(editorPart);
         editorPart.getEditorInput().getFile();
        this.actionManager.getEditorAPI().

        if (!isSharedEditor(editorPart)
                || !sarosSession.isShared(ResourceAdapterFactory.create(resource))
                || !editorAPI.getEditorResource(editorPart).isAccessible())
        {
            generateEditorActivated(null);
            // follower switched to another unshared editor or closed followed
            // editor (not shared editor gets activated)
            if (isFollowing())
            {
                setFollowing(null);
//                SarosView.showNotification(
//                                "Follow Mode stopped!",
//                                "You switched to another editor that is not shared \nor closed the followed editor.");
            }
            return;
        }

        *//*
         * If the opened editor is not the active editor of the user being
         * followed, then leave follow mode
         *//*
        if (isFollowing())
        {
            RemoteEditorManager.RemoteEditor activeEditor = remoteEditorManager.getEditorState(
                    getFollowedUser()).getActiveEditor();

            if (activeEditor != null
                    && !activeEditor.getPath().equals(
                    editorAPI.getEditorPath(editorPart)))
            {
                setFollowing(null);
                // follower switched to another shared editor or closed followed
                // editor (shared editor gets activated)
//                SarosView
//                        .showNotification("Follow Mode stopped!",
//                                "You switched to another editor \nor closed the followed editor.");
            }
        }

        SPath editorPath = this.editorAPI.getEditorPath(editorPart);
        ILineRange viewport = this.editorAPI.getViewport(editorPart);
        ITextSelection selection = this.editorAPI.getSelection(editorPart);

        // Set (and thus send) in this order:
        generateEditorActivated(editorPath);
        generateSelection(editorPart, selection);
        generateViewport(editorPart, viewport);

        if (viewport == null)
        {
            log.warn("Shared Editor does not have a Viewport: " + editorPart);
        }
        else
        {
            generateViewport(editorPart, viewport);
        }*/
//        ITextViewer viewer = EditorAPIEcl.getViewer(editorPart);
//
//        if (viewer instanceof ISourceViewer)
//        {
//            customAnnotationManager.installPainter((ISourceViewer) viewer);
//        }

    }

    @Override
    public void partOpened(IEditorPart editorPart)
    {
        System.out.println("EditorManager.partOpened E="+editorPart);
    }

    @Override
    public void partClosed(IEditorPart editor)
    {
        System.out.println("EditorManager.partClosed E="+editor);
    }

    @Override
    public void partInputChanged(IEditorPart editor)
    {
        System.out.println("EditorManager.partInputChanged E="+editor);
    }


    @Override
    public boolean isOpenEditor(SPath path)
    {
        return false;
    }

    @Override
    public void closeEditor(SPath path)
    {

    }

    @Override
    public void openEditor(SPath path)
    {

    }


}
