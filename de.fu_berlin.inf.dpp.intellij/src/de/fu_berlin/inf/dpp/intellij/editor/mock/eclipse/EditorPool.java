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

package de.fu_berlin.inf.dpp.intellij.editor.mock.eclipse;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.intellij.editor.mock.internal.EditorAPIEcl;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IDocument;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.ITextViewer;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IDocumentProvider;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IEditorInput;
import de.fu_berlin.inf.dpp.intellij.editor.mock.ui.ResourceUtil;
import org.apache.log4j.Logger;


import de.fu_berlin.inf.dpp.activities.SPath;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The EditorPool manages the IEditorParts of the local user. Currently only
 * those parts are supported by Saros (and thus managed in the EditorPool) which
 * can be traced to an {@link IFile} and an {@link de.fu_berlin.inf.dpp.intellij.editor.mock.text.ITextViewer}.
 */
class EditorPool {

    private static final Logger log = Logger.getLogger(EditorPool.class);

    protected EditorManagerEcl editorManager;

    EditorPool(EditorManagerEcl editorManager) {
        this.editorManager = editorManager;
    }

    /**
     * The editorParts-map will return all EditorParts associated with a given
     * IPath. This can be potentially many because a IFile (which can be
     * identified using a IPath) can be opened in multiple editors.
     */
    protected Map<SPath, HashSet<IEditorPart>> editorParts = new HashMap<SPath, HashSet<IEditorPart>>();

    /**
     * The editorInputMap contains all IEditorParts which are managed by the
     * EditorPool and stores the associated IEditorInput (the EditorInput could
     * also actually be retrieved directly from the IEditorPart).
     */
    protected Map<IEditorPart, IEditorInput> editorInputMap = new HashMap<IEditorPart, IEditorInput>();

    /**
     * Tries to add an {@link IEditorPart} to the {@link EditorPool}. This
     * method also connects the editorPart with its data source (identified by
     * associated {@link IFile}), makes it editable for user with
     * {@link Permission#WRITE_ACCESS}, and registers listeners:
     * <ul>
     * <li>{@link de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IElementStateListener} on {@link de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IDocumentProvider} - listens
     * for the changes in the file connected with the editor (e.g. file gets
     * 'dirty')</li>
     * <li>{@link de.fu_berlin.inf.dpp.intellij.editor.mock.text.IDocumentListener} on {@link de.fu_berlin.inf.dpp.intellij.editor.mock.text.IDocument} - listens for changes
     * in the document (e.g. documents text gets changed)</li>
     * <li>{@link EditorListener} on {@link IEditorPart} - listens for basic
     * events needed for tracking of text selection and viewport changes (e.g.
     * mouse events, keyboard events)</li>
     * </ul>
     *
     * This method will print a warning and return without any effect if the
     * given IEditorPart does not a.) represent an IFile, b.) which can be
     * referred to using an IPath and c.) the IEditorPart can be mapped to an
     * ITextViewer.
     *
     * The method is robust against adding the same IEditorPart twice.
     */
    public void add(IEditorPart editorPart) {

        SPath path = editorManager.editorAPI.getEditorPath(editorPart);

        if (path == null) {
            log.warn("Could not find path/resource for editor "
                    + editorPart.getTitle());
            return;
        }

        log.trace("EditorPool.add (" + path.toString() + ") invoked");

        if (getEditors(path).contains(editorPart)) {
            log.error("EditorPart was added twice to the EditorPool: "
                    + editorPart.getTitle(), new StackTrace());
            return;
        }

        ITextViewer viewer = EditorAPIEcl.getViewer(editorPart);
        if (viewer == null) {
            log.warn("This editor is not a ITextViewer: "
                    + editorPart.getTitle());
            return;
        }

        IEditorInput input = editorPart.getEditorInput();

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            log.warn("This editor does not use IFiles as input");
            return;
        }

        /*
         * Connecting causes Conversion of Delimiters which trigger Selection
         * and Save Activities, so connect before adding listeners
         */
        this.editorManager.connect(file);

        this.editorManager.editorAPI.addSharedEditorListener(
                this.editorManager, editorPart);

        this.editorManager.editorAPI.setEditable(editorPart,
                this.editorManager.hasWriteAccess && !this.editorManager.isLocked);

        IDocumentProvider documentProvider = EditorManagerEcl
                .getDocumentProvider(input);

        // TODO Not registering is very helpful to find errors related to file
        // transfer problems
        this.editorManager.dirtyStateListener.register(documentProvider, input);

        IDocument document = EditorManagerEcl.getDocument(editorPart);

        document.addDocumentListener(this.editorManager.documentListener);

        getEditors(path).add(editorPart);
        editorInputMap.put(editorPart, input);
    }

    public SPath getCurrentPath(IEditorPart editorPart,
            ISarosSession sarosSession) {

        IEditorInput input = editorInputMap.get(editorPart);
        if (input == null) {
            log.warn("EditorPart was never added to the EditorPool: "
                    + editorPart.getTitle());
            return null;
        }

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            log.warn("Could not find file for editor input "
                    + editorPart.getTitle());
            return null;
        }

        if (!sarosSession.isShared(ResourceAdapterFactory.create(file
                .getProject()))) {
            log.warn("File is from incorrect project: " + file.getProject()
                    + " should be " + sarosSession + ": " + file, new StackTrace());
        }

        IPath path = file.getProjectRelativePath();
        if (path == null) {
            log.warn("Could not find path for editor " + editorPart.getTitle());
        }
        return new SPath(ResourceAdapterFactory.create(file));
    }

    /**
     * Tries to removeAll an {@link IEditorPart} from {@link EditorPool}. This
     * Method also disconnects the editorPart from its data source (identified
     * by associated {@link IFile}) and removes registered listeners:
     * <ul>
     * <li>{@link de.fu_berlin.inf.dpp.intellij.editor.mock.ui.IElementStateListener} from {@link IDocumentProvider}</li>
     * <li>{@link de.fu_berlin.inf.dpp.intellij.editor.mock.text.IDocumentListener} from {@link IDocument}</li>
     * <li>{@link EditorListener} from {@link IEditorPart}</li>
     * </ul>
     *
     * This method also makes the Editor editable.
     *
     * @param editorPart
     *            editorPart to be removed
     *
     * @return {@link IPath} of the Editor that was removed from the Pool, or
     *         <code>null</code> on error.
     */
    public IPath remove(IEditorPart editorPart, ISarosSession sarosSession) {

        log.trace("EditorPool.removeAll " + editorPart + "invoked");

        IEditorInput input = editorInputMap.remove(editorPart);
        if (input == null) {
            log.warn("EditorPart was never added to the EditorPool: "
                    + editorPart.getTitle());
            return null;
        }

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            log.warn("Could not find file for editor input "
                    + editorPart.getTitle());
            return null;
        }

        if (!sarosSession.isShared(ResourceAdapterFactory.create(file
                .getProject()))) {
            log.warn("File is from incorrect project: " + file.getProject()
                    + " should be " + sarosSession + ": " + file, new StackTrace());
        }

        IPath path = file.getProjectRelativePath();
        if (path == null) {
            log.warn("Could not find path for editor " + editorPart.getTitle());
            return null;
        }

        // TODO Remove should removeAll empty HashSets
        if (!getEditors(new SPath(ResourceAdapterFactory.create(file))).remove(
                editorPart)) {
            log.error("EditorPart was never added to the EditorPool: "
                    + editorPart.getTitle());
            return null;
        }

        // Unregister and unhook
        this.editorManager.editorAPI.setEditable(editorPart, true);
        this.editorManager.editorAPI.removeSharedEditorListener(
                this.editorManager, editorPart);

        IDocumentProvider documentProvider = EditorManagerEcl
                .getDocumentProvider(input);
        this.editorManager.dirtyStateListener.unregister(documentProvider,
                input);

        IDocument document = documentProvider.getDocument(input);
        if (document == null) {
            log.warn("Could not disconnect from document: " + path);
        } else {
            document
                    .removeDocumentListener(this.editorManager.documentListener);
        }

        this.editorManager.disconnect(file);

        return path;
    }

    /**
     * Returns all IEditorParts which have been added to this IEditorPool which
     * display a file using the given path.
     *
     * @param path
     *            {@link IPath} of the Editor
     *
     * @return set of relating IEditorPart
     *
     */
    public Set<IEditorPart> getEditors(SPath path) {

        log.trace(".getEditors(" + path.toString() + ") invoked");
        if (!editorParts.containsKey(path)) {
            HashSet<IEditorPart> result = new HashSet<IEditorPart>();
            editorParts.put(path, result);
            return result;
        }
        return editorParts.get(path);
    }

    /**
     * Returns all IEditorParts actually managed in the EditorPool.
     *
     * @return set of all {@link IEditorPart} from the {@link EditorPool}.
     *
     */
    public Set<IEditorPart> getAllEditors() {

        log.trace("EditorPool.getAllEditors invoked");

        Set<IEditorPart> result = new HashSet<IEditorPart>();

        for (Set<IEditorPart> parts : this.editorParts.values()) {
            result.addAll(parts);
        }
        return result;
    }

    /**
     * Removes all {@link IEditorPart} from the EditorPool.
     */
    public void removeAllEditors(ISarosSession sarosSession) {

        log.trace("EditorPool.removeAllEditors invoked");

        for (IEditorPart part : new HashSet<IEditorPart>(getAllEditors())) {
            remove(part, sarosSession);
        }

        assert getAllEditors().size() == 0;
    }

    /**
     * Will set all IEditorParts in the EditorPool to be editable by the local
     * user if has {@link Permission#WRITE_ACCESS}. The editors will be locked
     * otherwise.
     */
    public void setWriteAccessEnabled(boolean hasWriteAccess) {

        log.trace("EditorPool.setEditable");

        for (IEditorPart editorPart : getAllEditors()) {
            this.editorManager.editorAPI
                    .setEditable(editorPart, hasWriteAccess);
        }
    }

    /**
     * Will set all IEditorParts that are opened editable or non-editable. This
     * method is not limited to shared editors.
     *
     * @param editable
     */
    public void setLocalEditorsEnabled(boolean editable) {

        log.trace("EditorPool.setEditable");

        for (IEditorPart editorPart : EditorAPIEcl.getOpenEditors()) {
            this.editorManager.editorAPI.setEditable(editorPart, editable);
        }
    }

    /**
     * Returns true iff the given IEditorPart is managed by the
     * {@link EditorPool}. See EditorPool for a description of which
     * IEditorParts are managed.
     */
    public boolean isManaged(IEditorPart editor) {
        return editorInputMap.containsKey(editor);
    }
}
