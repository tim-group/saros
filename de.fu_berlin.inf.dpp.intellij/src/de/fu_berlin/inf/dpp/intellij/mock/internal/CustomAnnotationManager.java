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

import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IDrawingStrategy;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.IPainter;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.ISourceViewer;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.Annotation;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.AnnotationPainter;
import de.fu_berlin.inf.dpp.intellij.editor.mock.text.DefaultMarkerAnnotationAccess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


// TODO support color changes for annotations
public class CustomAnnotationManager {

    private Map<ISourceViewer, AnnotationPainter> installedPainters = new HashMap<ISourceViewer, AnnotationPainter>();

    private Map<String, IDrawingStrategy> drawingStrategyForAnnotationType = new HashMap<String, IDrawingStrategy>();

    private Map<String, Integer> customAnnotationTypes = new HashMap<String, Integer>();

    public static class CustomMarkerAnnotationAccess extends
            DefaultMarkerAnnotationAccess
    {

        private Map<String, Integer> annotationLayerInformation;

        public CustomMarkerAnnotationAccess(
                Map<String, Integer> annotationLayerInformation) {
            this.annotationLayerInformation = new HashMap<String, Integer>(
                    annotationLayerInformation);
        }

        @Override
        public int getLayer(Annotation annotation) {
            Integer layer = annotationLayerInformation
                    .get(annotation.getType());

            return layer == null ? 0 : layer;
        }
    }

    /**
     * Installs a custom {@link AnnotationPainter annotation painter} to the
     * given source viewer. If there is already an custom annotation painter
     * installed this method just returns.
     *
     * @param sourceViewer
     */
    public void installPainter(ISourceViewer sourceViewer) {

        AnnotationPainter painter = installedPainters.get(sourceViewer);

        if (painter != null)
            return;

        painter = new AnnotationPainter(sourceViewer,
                new CustomMarkerAnnotationAccess(customAnnotationTypes));

        for (String annotationType : customAnnotationTypes.keySet()) {
            IDrawingStrategy strategy = drawingStrategyForAnnotationType
                    .get(annotationType);

            if (strategy == null)
                continue;

            painter.addAnnotationType(annotationType, annotationType);
            painter.addDrawingStrategy(annotationType, strategy);
            // set a color or the drawing strategy will not be invoked
            // FIMXE no control when this color is disposed
            painter.setAnnotationTypeColor(annotationType, sourceViewer
                    .getTextWidget().getForeground());
        }

        painter.paint(IPainter.CONFIGURATION);
    }

    /**
     * Uninstalls the custom {@link AnnotationPainter annotation painter} from
     * the given source viewer. If there is no custom annotation painter
     * installed this method just returns.
     *
     * @param sourceViewer
     * @param redraw
     */
    public void uninstallPainter(ISourceViewer sourceViewer, boolean redraw) {

        AnnotationPainter painter = installedPainters.remove(sourceViewer);

        if (painter == null)
            return;

        painter.deactivate(redraw);
        painter.dispose();
    }

    /**
     * Uninstalls all custom {@link AnnotationPainter annotation painters} that
     * were installed by {@link #installPainter(ISourceViewer)}.
     *
     * @param redraw
     */
    public void uninstallAllPainters(boolean redraw) {
        for (ISourceViewer sourceViewer : Arrays.asList(installedPainters
                .keySet().toArray(new ISourceViewer[0])))
            uninstallPainter(sourceViewer, redraw);

    }

    public void registerDrawingStrategy(String annotationType,
            IDrawingStrategy strategy) {
        drawingStrategyForAnnotationType.put(annotationType, strategy);
    }

    public void registerAnnotation(String annotationType, int layer) {
        customAnnotationTypes.put(annotationType, layer);
    }
}
