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
import de.fu_berlin.inf.dpp.core.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.FileEditorInput;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.IDocumentProvider;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorInput;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.IElementStateListener;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import de.fu_berlin.inf.dpp.intellij.util.AutoHashMap;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Listener registered on Editors to be informed about their dirty state.
 * <p/>
 * There is one global DirtyStateListener for all editors!
 */
public class DirtyStateListener implements IElementStateListener
{

    private static final Logger log = Logger
            .getLogger(DirtyStateListener.class);

    protected final EditorManager editorManager;

    public DirtyStateListener(EditorManager editorManager)
    {
        this.editorManager = editorManager;
    }

    public boolean enabled = true;

    @Override
    public void elementDirtyStateChanged(Object element, boolean isDirty)
    {

        if (!enabled)
        {
            return;
        }

        if (isDirty)
        {
            return;
        }

        if (!this.editorManager.hasWriteAccess)
        {
            return;
        }

        if (!(element instanceof FileEditorInput))
        {
            return;
        }

        final IFile file = ((FileEditorInput) element).getFile();
        final ISarosSession sarosSession = editorManager.sarosSession;

        if (sarosSession == null
                || !sarosSession.isShared(ResourceAdapterFactory.create(file
                .getProject())))
        {
            return;
        }

        SWTUtils.runSafeSWTSync(log, new Runnable()
        {

            @Override
            public void run()
            {

                // Only trigger save events for files managed in the editor pool
                if (!editorManager.isConnected(file))
                {
                    return;
                }

                EditorManager.log.debug("Dirty state reset for: "
                        + file.toString());
                editorManager.sendEditorActivitySaved(new SPath(
                        ResourceAdapterFactory.create(file)));
            }
        });
    }

    @Override
    public void elementContentAboutToBeReplaced(Object element)
    {
        // ignore
    }

    @Override
    public void elementContentReplaced(Object element)
    {
        // ignore
    }

    @Override
    public void elementDeleted(Object element)
    {
        // ignore
    }

    @Override
    public void elementMoved(Object originalElement, Object movedElement)
    {
        // ignore
    }

    AutoHashMap<IDocumentProvider, Set<IEditorInput>> documentProviders = AutoHashMap
            .getSetHashMap();

    public void register(IDocumentProvider documentProvider, IEditorInput input)
    {

        Set<IEditorInput> inputs = documentProviders.get(documentProvider);
        if (inputs.size() == 0)
        {
            documentProvider.addElementStateListener(this);
        }
        inputs.add(input);
    }

    public void unregister(IDocumentProvider documentProvider,
            IEditorInput input)
    {

        Set<IEditorInput> inputs = documentProviders.get(documentProvider);
        inputs.remove(input);
        if (inputs.size() == 0)
        {
            documentProvider.removeElementStateListener(this);
            documentProviders.remove(documentProvider);
        }
    }

    public void unregisterAll()
    {

        for (IDocumentProvider provider : documentProviders.keySet())
        {
            log.warn("DocumentProvider was not correctly"
                    + " unregistered yet, EditorPool must be corrupted: "
                    + documentProviders);
            provider.removeElementStateListener(this);
        }
        documentProviders.clear();
    }

}