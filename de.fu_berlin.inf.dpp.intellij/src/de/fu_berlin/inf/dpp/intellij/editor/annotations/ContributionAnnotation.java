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

package de.fu_berlin.inf.dpp.intellij.editor.annotations;

import de.fu_berlin.inf.dpp.intellij.mock.editor.text.IAnnotationModel;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * Marks text contributions done by a user with {@link Permission#WRITE_ACCESS}.
 * <p/>
 * Configuration of this annotation is done in the plugin-xml.
 *
 * @author rdjemili
 */
public class ContributionAnnotation extends SarosAnnotation
{

    protected static final String TYPE = "de.fu_berlin.inf.dpp.annotations.contribution";

    /**
     * The treeModel this annotation belongs to.
     */
    protected IAnnotationModel model;

    public ContributionAnnotation(User source, IAnnotationModel model)
    {
        super(ContributionAnnotation.TYPE, true, "Text contributed by "
                + source.getNickname(), source);
        this.model = model;
    }

    public IAnnotationModel getModel()
    {
        return model;
    }
}
