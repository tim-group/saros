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

package de.fu_berlin.inf.dpp.intellij.editor.mock.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IAnnotationModel;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IAnnotationModelExtension;
import de.fu_berlin.inf.dpp.intellij.editor.mock.events.IPropertyChangeListener;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.ContributionAnnotation;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.Annotation;
import de.fu_berlin.inf.dpp.intellij.editor.mock.Position;
import de.fu_berlin.inf.dpp.intellij.editor.mock.events.PropertyChangeEvent;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.SWTUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;


/**
 * This class keeps a history of added {@link ContributionAnnotation}s and
 * removes old ones.
 */
public class ContributionAnnotationManager {

    private static final Logger log = Logger
            .getLogger(ContributionAnnotationManager.class);

    static final int MAX_HISTORY_LENGTH = 20;

    private final Map<User, LinkedList<ContributionAnnotation>> sourceToHistory = new HashMap<User, LinkedList<ContributionAnnotation>>();

    private final ISarosSession sarosSession;

    private final IPreferenceStore preferenceStore;

    private boolean contribtionAnnotationsEnabled;

    private final ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(User user) {
            /*
             * Just removeAll the annotations from the history. They are removed by
             * the EditorManagerEcl from the editors.
             */
            sourceToHistory.remove(user);
        }
    };

    private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {

            if (!PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS.equals(event
                    .getProperty()))
                return;

            SWTUtils.runSafeSWTAsync(log, new Runnable()
            {

                @Override
                public void run()
                {
                    contribtionAnnotationsEnabled = Boolean.valueOf(event
                            .getNewValue().toString());

                    if (!contribtionAnnotationsEnabled)
                        removeAllAnnotations();
                }
            });
        }
    };

    public ContributionAnnotationManager(ISarosSession sarosSession,
            IPreferenceStore preferenceStore) {
        this.sarosSession = sarosSession;
        this.preferenceStore = preferenceStore;
        this.preferenceStore.addPropertyChangeListener(propertyChangeListener);
        this.sarosSession.addListener(sharedProjectListener);
        contribtionAnnotationsEnabled = this.preferenceStore
                .getBoolean(PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS);
    }

    /**
     * Inserts a contribution annotation to given treeModel if there is not already
     * a contribution annotation at given position. This method should be called
     * after the text has changed.
     *
     * @param model
     *            to add the annotation to.
     * @param offset
     *            first of the annotation to add.
     * @param length
     *            length of the annotation.
     * @param source
     *            of the annotation.
     */
    @SuppressWarnings("unchecked")
    public void insertAnnotation(IAnnotationModel model, int offset,
            int length, User source) {

        if (!contribtionAnnotationsEnabled)
            return;

        if (length > 0) {
            /* Return early if there already is an annotation at that offset */
            for (Iterator<Annotation> it = model.getAnnotationIterator(); it
                    .hasNext();) {
                Annotation annotation = it.next();

                if (annotation instanceof ContributionAnnotation
                        && model.getPosition(annotation).includes(offset)
                        && ((ContributionAnnotation) annotation).getSource()
                        .equals(source)) {
                    return;
                }
            }

            ContributionAnnotation annotation = new ContributionAnnotation(
                    source, model);
            addContributionAnnotation(annotation, new Position(offset, length));
        }
    }

    /**
     * Splits the contribution annotation at given position, so that the
     * following text change won't expand the annotation. This needs to be
     * called before the text is changed.
     *
     * @param model
     *            to search for annotations to split.
     * @param offset
     *            at which annotations should be splitted.
     */

    @SuppressWarnings("unchecked")
    public void splitAnnotation(IAnnotationModel model, int offset) {

        if (!contribtionAnnotationsEnabled)
            return;

        for (Iterator<Annotation> it = model.getAnnotationIterator(); it
                .hasNext();) {
            Annotation annotation = it.next();

            if (annotation instanceof ContributionAnnotation) {

                Position pos = model.getPosition(annotation);

                if (pos == null) {
                    /*
                     * FIXME This error occurs when search/replacing lots of
                     * small stuff as client
                     */
                    log.warn("Annotation could not be found: " + annotation);
                    return;
                }

                if ((offset > pos.offset) && (offset < pos.offset + pos.length)) {
                    Position beforeOffset = new Position(pos.offset, offset
                            - pos.offset);
                    Position afterOffset = new Position(offset, pos.length
                            - (offset - pos.offset));

                    ContributionAnnotation oldAnnotation = (ContributionAnnotation) annotation;

                    removeFromHistory(oldAnnotation);

                    ContributionAnnotation newAnnotation;
                    User source = oldAnnotation.getSource();

                    newAnnotation = new ContributionAnnotation(source, model);
                    addContributionAnnotation(newAnnotation, beforeOffset);

                    newAnnotation = new ContributionAnnotation(source, model);
                    addContributionAnnotation(newAnnotation, afterOffset);
                }
            }
        }
    }

    /**
     * Refreshes all contribution annotations in the treeModel by removing and
     * reinserting them.
     *
     * @param model
     *            the annotation treeModel that should be refreshed
     */
    @SuppressWarnings("unchecked")
    public void refreshAnnotations(IAnnotationModel model) {
        List<Annotation> annotationsToRemove = new ArrayList<Annotation>();
        Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();

        for (Iterator<Annotation> it = model.getAnnotationIterator(); it
                .hasNext();) {

            Annotation annotation = it.next();

            if (!(annotation instanceof ContributionAnnotation))
                continue;

            Position position = model.getPosition(annotation);

            if (position == null) {
                log.warn("annotation could not be found in the current treeModel: "
                        + annotation);
                continue;
            }

            /*
             * we rely on the fact the a user object is unique during a running
             * session so that user.equals(user) <=> user == user otherwise just
             * reinserting the annotations would not refresh the colors as the
             * color id of the user has not changed
             */
            annotationsToRemove.add(annotation);
            ContributionAnnotation annotationToAdd = new ContributionAnnotation(
                    ((ContributionAnnotation) annotation).getSource(), model);

            annotationsToAdd.put(annotationToAdd, position);

            replaceInHistory((ContributionAnnotation) annotation,
                    annotationToAdd);
        }

        if (annotationsToRemove.isEmpty())
            return;

        if (model instanceof IAnnotationModelExtension) {
            ((IAnnotationModelExtension) model).replaceAnnotations(
                    annotationsToRemove.toArray(new Annotation[0]),
                    annotationsToAdd);

            return;
        }

        for (Annotation annotation : annotationsToRemove)
            model.removeAnnotation(annotation);

        for (Map.Entry<Annotation, Position> entry : annotationsToAdd
                .entrySet())
            model.addAnnotation(entry.getKey(), entry.getValue());
    }

    public void dispose() {
        sarosSession.removeListener(sharedProjectListener);
        preferenceStore.removePropertyChangeListener(propertyChangeListener);
        sourceToHistory.clear();
    }

    /**
     * Get the history of contribution annotations of the given user.
     *
     * @param source
     *            source of the user who's history we want.
     * @return the history of source.
     */
    private Queue<ContributionAnnotation> getHistory(User source) {
        LinkedList<ContributionAnnotation> result = sourceToHistory.get(source);
        if (result == null) {
            result = new LinkedList<ContributionAnnotation>();
            sourceToHistory.put(source, result);
        }
        return result;
    }

    /**
     * Add a contribution annotation to the annotation treeModel and store it into
     * the history of the associated user. Old entries are removed from the
     * history and the annotation treeModel.
     */
    private void addContributionAnnotation(ContributionAnnotation annotation,
            Position position) {

        annotation.getModel().addAnnotation(annotation, position);

        Queue<ContributionAnnotation> history = getHistory(annotation
                .getSource());
        history.add(annotation);
        while (history.size() > MAX_HISTORY_LENGTH) {
            ContributionAnnotation oldAnnotation = history.remove();
            oldAnnotation.getModel().removeAnnotation(oldAnnotation);
        }
    }

    /**
     * Removes an annotation from the user's history and the annotation treeModel.
     *
     * @param annotation
     */
    private void removeFromHistory(ContributionAnnotation annotation) {
        getHistory(annotation.getSource()).remove(annotation);
        annotation.getModel().removeAnnotation(annotation);
    }

    /**
     * Replaces an existing annotation in the current history with a new
     * annotation.
     *
     * @param oldAnnotation
     * @param newAnnotation
     */
    private void replaceInHistory(ContributionAnnotation oldAnnotation,
            ContributionAnnotation newAnnotation) {
        assert oldAnnotation.getSource().equals(newAnnotation.getSource());

        LinkedList<ContributionAnnotation> list = sourceToHistory
                .get(oldAnnotation.getSource());

        if (list == null) {
            log.warn("a annotation history for user "
                    + oldAnnotation.getSource() + " does not exists");

            return;
        }

        for (ListIterator<ContributionAnnotation> it = list.listIterator(); it
                .hasNext();) {
            ContributionAnnotation annotation = it.next();
            if (annotation.equals(oldAnnotation)) {
                it.set(newAnnotation);
                return;
            }
        }

        log.warn("could not find annotation " + oldAnnotation
                + " in the current history for user: " + oldAnnotation.getSource());
    }

    private void removeAllAnnotations() {
        for (Queue<ContributionAnnotation> queue : sourceToHistory.values())
            while (!queue.isEmpty())
                removeFromHistory(queue.peek());
    }
}
