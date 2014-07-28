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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for handling activities on local editors and transforming them to calls to
 * {@link de.fu_berlin.inf.dpp.core.editor.EditorManager} for generating activities .
 */
public class LocalEditorHandler {

    private static final Logger LOG = Logger
        .getLogger(LocalEditorHandler.class);

    private final ProjectAPI projectAPI;
    /**
     * This is just a reference to {@link de.fu_berlin.inf.dpp.core.editor.EditorManager}'s editorPool and not a
     * separate pool.
     */
    private EditorPool editorPool;

    private EditorManager manager;

    private Map<VirtualFile, byte[]> newFiles = new HashMap<VirtualFile, byte[]>();

    public LocalEditorHandler(ProjectAPI projectAPI) {
        this.projectAPI = projectAPI;
    }

    /**
     * Initializes all fields that require an EditorManager.
     *
     * @param manager
     */
    public void initialize(EditorManager manager) {
        this.editorPool = manager.getEditorPool();
        this.manager = manager;
        projectAPI.addFileEditorManagerListener(manager.getFileListener());
    }

    /**
     * Adds the opened file to the editorPool and calls
     * {@link de.fu_berlin.inf.dpp.core.editor.EditorManager#startEditor(com.intellij.openapi.editor.Editor)}
     * on the opened Editor. Additionally it sends the file's content via
     * {@link EditorManager#generateTextEdit(int, String, String, de.fu_berlin.inf.dpp.activities.SPath)},
     * when it is newly created local file.
     *
     * @param virtualFile
     */
    public void openEditor(VirtualFile virtualFile) {
        SPath path = toPath(virtualFile);
        if (path != null) {
            editorPool.add(path, projectAPI.getActiveEditor());
            manager.startEditor(projectAPI.getActiveEditor());
        }
    }

    /**
     * Removes a file from the editorPool and calls
     * {@link de.fu_berlin.inf.dpp.core.editor.EditorManager#generateEditorClosed(de.fu_berlin.inf.dpp.activities.SPath)}
     *
     * @param virtualFile
     */
    public void closeEditor(VirtualFile virtualFile) {
        SPath path = toPath(virtualFile);
        if (path != null) {
            editorPool.removeEditor(path);
            manager.generateEditorClosed(path);
        }
    }

    /**
     * Saves the document under path..
     *
     * @param path
     */
    public void saveFile(SPath path) {
        Document doc = editorPool.getDocument(path);
        if (doc != null) {
            projectAPI.saveDocument(doc);
        } else {
            LOG.warn("Document does not exist: " + path);
        }
    }

    /**
     * Calls {@link EditorManager#generateEditorActivated(de.fu_berlin.inf.dpp.activities.SPath)}.
     *
     * @param file
     */
    public void activateEditor(VirtualFile file) {
        SPath path = toPath(file);
        if (path != null) {
            manager.generateEditorActivated(path);
        }
    }

    //FIXME: not sure how to do it intelliJ
    public void sendEditorActivitySaved(SPath path) {

    }

    /**
     * Adds a newly created file to the newFile list for sending it when it is opened.
     *
     * @param virtualFile
     * @param content
     */
    public void registerNewFile(VirtualFile virtualFile, byte[] content) {
        newFiles.put(virtualFile, content);
    }

    /**
     * @param path
     * @return <code>true</code>, if the path is opened in an editor.
     */
    public boolean isOpenEditor(SPath path) {
        Document doc = editorPool.getDocument(path);
        if (doc == null) {
            return false;
        }

        return projectAPI.isOpen(doc);
    }

    private SPath toPath(VirtualFile virtualFile) {
        if (virtualFile == null || !virtualFile.exists() || !manager
            .hasSession()) {
            return null;
        }

        IResource resource = null;
        String path = virtualFile.getPath();

        for (IProject project : manager.getSession().getProjects()) {
            resource = project.getFile(path);
            if (resource != null) {
                break;
            }

        }
        return resource == null ? null : new SPath(resource);
    }
}
