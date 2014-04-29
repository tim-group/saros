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

package de.fu_berlin.inf.dpp.intellij.mock.internal;

import de.fu_berlin.inf.dpp.core.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.ITextViewer;
import de.fu_berlin.inf.dpp.intellij.editor.mock.events.*;
import de.fu_berlin.inf.dpp.intellij.editor.mock.*;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.ITextListener;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.TextSelection;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.events.TextEvent;
import org.apache.log4j.Logger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;


/**
 * Listener for tracking the selection and viewport of an IEditorPart and
 * reporting any changes to an EditorManagerEcl.
 */
public class EditorListener {

    private static final Logger log = Logger.getLogger(EditorListener.class
            .getName());

    protected final IEditorManager manager;

    protected ITextViewer viewer;

    protected IEditorPart part;

    protected ITextSelection lastSelection = new TextSelection(-1, -1);

    protected ILineRange lastViewport = new LineRange(-1, -1);

    protected boolean isUnsupportedEditor;

    public EditorListener(IEditorManager manager) {
        this.manager = manager;
    }

    /**
     * Connects all selection listeners to the given {@linkplain IEditorPart
     * editor part}. If an editor part was already bound it will be unbound and
     * replaced with the given editor part.
     *
     * @see #unbind()
     *
     * @param part
     *            the editor part to observe
     * @return <code>true</code> if the selection listeners were successfully
     *         installed, <code>false</code> if the selection listeners could
     *         not be installed
     */
    public boolean bind(final IEditorPart part) {

        if (this.part != null)
            unbind();

        final ITextViewer viewer = EditorAPIEcl.getViewer(part);

        if (viewer == null) {
            log.warn("could not attach selection listeners to editor part:"
                    + part + " , could not retrieve text widget");
            return false;
        }

        this.part = part;
        this.viewer = viewer;

        final StyledText textWidget = viewer.getTextWidget();

        textWidget.addControlListener(controlListener);
        textWidget.addMouseListener(mouseListener);
        textWidget.addKeyListener(keyListener);

        viewer.addTextListener(textListener);
        viewer.getSelectionProvider().addSelectionChangedListener(selectionChangedListener);
        viewer.addViewportListener(viewportListener);

        return true;
    }

    /**
     * Disconnects all selection listeners from the underlying
     * {@linkplain IEditorPart editor part}.
     *
     * @see #bind(IEditorPart)
     *
     */
    public void unbind() {

        if (part == null)
            return;

        StyledText textWidget = viewer.getTextWidget();
        textWidget.removeControlListener(controlListener);
        textWidget.removeMouseListener(mouseListener);
        textWidget.removeKeyListener(keyListener);

        viewer.getSelectionProvider().removeSelectionChangedListener(selectionChangedListener);
        viewer.removeViewportListener(viewportListener);
        viewer.removeTextListener(textListener);

        viewer = null;
        part = null;
    }

    /**
     * Listens to resize events of the control, because the IViewportListener
     * does not report such events.
     */
    protected ControlListener controlListener = new ControlListener() {

        @Override
        public void controlMoved(ControlEvent e) {
            generateViewport();
        }

        @Override
        public void controlResized(ControlEvent e) {
            generateViewport();
        }

    };

    protected final MouseListener mouseListener = new MouseListener() {

        @Override
        public void mouseDown(MouseEvent e) {
            generateSelection();
        }

        @Override
        public void mouseUp(MouseEvent e) {
            generateSelection();
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
            // ignore
        }
    };

    protected final KeyListener keyListener = new KeyListener() {
        @Override
        public void keyReleased(KeyEvent e) {
            generateSelection();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // ignore
        }

        @Override
        public void keyTyped(KeyEvent e)
        {

        }
    };

    protected IViewportListener viewportListener = new IViewportListener() {
        /*
         * This does not report window resizes because of
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=171018
         */
        @Override
        public void viewportChanged(int verticalOffset) {
            generateViewport();
        }
    };

    protected ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            generateSelection();
        }
    };

    /**
     * Listens to newlines being inserted or deleted to inform our listener of
     * updates to the view port
     */
    protected ITextListener textListener = new ITextListener() {

        @Override
        public void textChanged(TextEvent event) {

            String text = event.getText();
            String replaced = event.getReplacedText();
            if ((text != null && text.indexOf('\n') != -1)
                    || (replaced != null && replaced.indexOf('\n') != -1)) {
                generateViewport();
            }
        }
    };

    protected void generateSelection() {

        ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

        if (!this.lastSelection.equals(selection)) {
            this.lastSelection = selection;
            this.manager.generateSelection(this.part, selection);
        }
    }

    public boolean equals(ILineRange one, ILineRange two) {

        if (one == null)
            return two == null;

        if (two == null)
            return false;

        return one.getNumberOfLines() == two.getNumberOfLines()
                && one.getStartLine() == two.getStartLine();
    }

    protected void generateViewport() {

        ILineRange viewport = EditorAPIEcl.getViewport(viewer);

        if (!equals(viewport, lastViewport)) {
            lastViewport = viewport;
            if (log.isDebugEnabled() && viewport != null) {
                log.debug("Viewport changed: " + viewport.getStartLine() + "+"
                        + viewport.getNumberOfLines());
            }
            manager.generateViewport(part, viewport);
        }
    }
}