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

package de.fu_berlin.inf.dpp.intellij.mock.editor.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.fu_berlin.inf.dpp.core.editor.internal.IEditorPart;
import de.fu_berlin.inf.dpp.intellij.mock.editor.EditorManagerEcl;
import de.fu_berlin.inf.dpp.intellij.mock.editor.text.Annotation;
import de.fu_berlin.inf.dpp.intellij.mock.editor.text.IAnnotationModel;
import de.fu_berlin.inf.dpp.intellij.mock.editor.text.IAnnotationModelExtension;
import de.fu_berlin.inf.dpp.intellij.mock.editor.Position;
import de.fu_berlin.inf.dpp.intellij.mock.editor.ui.IDocumentProvider;
import de.fu_berlin.inf.dpp.core.editor.internal.IEditorInput;
import de.fu_berlin.inf.dpp.intellij.util.Predicate;
import org.apache.log4j.Logger;


/**
 * This class holds convenience methods for managing annotations.
 */
public class AnnotationModelHelper {

    private static final Logger LOG = Logger
            .getLogger(AnnotationModelHelper.class);

    private static Iterable<Annotation> toIterable(final IAnnotationModel model) {
        return new Iterable<Annotation>() {
            @Override
            @SuppressWarnings("unchecked")
            public Iterator<Annotation> iterator() {
                return model.getAnnotationIterator();
            }
        };
    }

    /**
     * Removes annotations that match a given predicate.
     */
    public void removeAnnotationsFromEditor(IEditorPart editor,
            Predicate<Annotation> predicate) {
        IAnnotationModel model = retrieveAnnotationModel(editor);

        if (model == null) {
            return;
        }

        removeAnnotationsFromModel(model, predicate);
    }

    /**
     * Removes annotations that match a given predicate.
     *
     * @param model
     *            The {@link IAnnotationModel} that should be cleaned.
     * @param predicate
     *            The filter to use for cleaning.
     */
    public void removeAnnotationsFromModel(IAnnotationModel model,
            Predicate<Annotation> predicate) {

        Map<Annotation, Position> replacement = Collections.emptyMap();

        replaceAnnotationsInModel(model, predicate, replacement);
    }

    /**
     * Removes annotations that match a given predicate and replaces them in one
     * step.
     *
     * @param model
     *            The {@link IAnnotationModel} that should be cleaned.
     * @param predicate
     *            The filter to use for cleaning.
     */
    public void replaceAnnotationsInModel(IAnnotationModel model,
            Predicate<Annotation> predicate, Map<Annotation, Position> replacement) {

        // Collect annotations.
        ArrayList<Annotation> annotationsToRemove = new ArrayList<Annotation>(
                128);

        for (Annotation annotation : AnnotationModelHelper.toIterable(model)) {
            if (predicate.evaluate(annotation)) {
                annotationsToRemove.add(annotation);
            }
        }

        // Remove collected annotations.
        if (model instanceof IAnnotationModelExtension) {
            IAnnotationModelExtension extension = (IAnnotationModelExtension) model;
            extension.replaceAnnotations(annotationsToRemove
                            .toArray(new Annotation[annotationsToRemove.size()]),
                    replacement);
        } else {
            LOG.trace("AnnotationModel does not "
                    + "support IAnnotationModelExtension: " + model);

            for (Annotation annotation : annotationsToRemove) {
                model.removeAnnotation(annotation);
            }

            for (Entry<Annotation, Position> entry : replacement.entrySet()) {
                model.addAnnotation(entry.getKey(), entry.getValue());
            }
        }
    }

    public IAnnotationModel retrieveAnnotationModel(IEditorPart editorPart) {
        IEditorInput input = editorPart.getEditorInput();
        IDocumentProvider provider = EditorManagerEcl.getDocumentProvider(input);
        IAnnotationModel model = provider.getAnnotationModel(input);

        return model;
    }
}
