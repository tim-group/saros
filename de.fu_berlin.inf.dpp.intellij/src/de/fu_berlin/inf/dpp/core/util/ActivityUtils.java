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

package de.fu_berlin.inf.dpp.core.util;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;

import java.util.*;

/**
 * Class contains static helper methods for {@link IActivityDataObject ADOs}.
 */
public class ActivityUtils {

    /**
     * Checks if the give collections contains only
     * {@linkplain ChecksumActivityDataObject checksum ADOs}.
     * 
     * @param activities
     *            collection containing {@linkplain IActivityDataObject ADOs}
     * @return <code>true</code> if the collection contains only checksum ADOs,
     *         <code>false</code> otherwise
     */
    public static boolean containsChecksumsOnly(
        Collection<IActivityDataObject> activities) {

        if (activities.isEmpty())
            return false;

        for (IActivityDataObject a : activities)
            if (!(a instanceof ChecksumActivityDataObject))
                return false;

        return true;
    }

    /**
     * Tries to reduce the number of {@link IActivityDataObject ADOs} so that:
     * <p/>
     * <p/>
     * 
     * <pre>
     * for (activity : optimize(activities))
     *         exec(activity)
     * 
     * will produce the same result as
     * 
     * for (activity : activities)
     *         exec(activity)
     * </pre>
     * 
     * @param activities
     *            a collection containing the ADOs to optimize
     * @return a list which may contains a reduced amount of ADOs
     */

    public static List<IActivityDataObject> optimize(
        Collection<IActivityDataObject> activities) {

        List<IActivityDataObject> result = new ArrayList<IActivityDataObject>(
            activities.size());

        boolean[] dropDAOIdx = new boolean[activities.size()];

        Map<SPathDataObject, Integer> selections = new HashMap<SPathDataObject, Integer>();
        Map<SPathDataObject, Integer> viewports = new HashMap<SPathDataObject, Integer>();

        /*
         * keep only the latest selection/viewport activities per project and
         * path
         */

        int daoIdx = 0;

        for (IActivityDataObject dao : activities) {

            if (dao instanceof TextSelectionActivityDataObject) {

                SPathDataObject daoPath = ((TextSelectionActivityDataObject) dao)
                    .getPath();

                Integer idx = selections.get(daoPath);

                if (idx != null)
                    dropDAOIdx[idx] = true;

                selections.put(daoPath, daoIdx);
            } else if (dao instanceof ViewportActivityDataObject) {
                SPathDataObject daoPath = ((ViewportActivityDataObject) dao)
                    .getPath();

                Integer idx = viewports.get(daoPath);

                if (idx != null)
                    dropDAOIdx[idx] = true;

                viewports.put(daoPath, daoIdx);
            }

            daoIdx++;
        }

        daoIdx = 0;

        for (IActivityDataObject dao : activities)
            if (!dropDAOIdx[daoIdx++])
                result.add(dao);

        return result;
    }
}
