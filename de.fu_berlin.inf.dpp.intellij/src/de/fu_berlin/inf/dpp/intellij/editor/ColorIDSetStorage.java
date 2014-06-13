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

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.editor.colorstorage.ColorIDSet;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * This class stores {@link ColorIDSet}s when ever a ColorIdSet is added it gets
 * saved to a preference store
 */

@Component(module = "core")
public final class ColorIDSetStorage {

    private static final Logger LOG = Logger.getLogger(ColorIDSetStorage.class);

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    private static final String PREFERENCE_STORE_KEY = "de.fu_berlin.inf.dpp.coloridsets";

    private static final long REMOVE_THRESHOLD = 1000L * 60L * 60L * 24L * 30L;

    private List<ColorIDSet> currentAvailableSets;

    private IPreferenceStore preferenceStore;

    /**
     * Creates a new ColorIdSetStorage giving a preference store for storing a
     * string which represents the sets in memory
     *
     * @param prefStore
     */
    public ColorIDSetStorage(IPreferenceStore prefStore) {
        this.preferenceStore = prefStore;
        load();
        remove(REMOVE_THRESHOLD);
    }

    /**
     * Returns the matching {@link ColorIDSet} for the given ids if possible.
     *
     * @param ids
     *            the ids to lookup
     * @return {@link ColorIDSet} or a new set that includes all ids if no
     *         matching set was found
     *
     */
    public synchronized ColorIDSet getColorIDSet(Collection<String> ids) {
        return getColorIDSet(ids, Collections.<String> emptyList());
    }

    /**
     * Returns the matching {@link ColorIDSet} for the given ids if possible.
     *
     * @param ids
     *            the ids to lookup
     * @param additionalIds
     *            additional ids to add
     * @return {@link ColorIDSet} or a new set that includes all ids and
     *         additionalIds if no matching set was found
     *
     */
    public synchronized ColorIDSet getColorIDSet(Collection<String> ids,
                                                 Collection<String> additionalIds) {

        Set<String> allIDs = new HashSet<String>();

        allIDs.addAll(ids);
        allIDs.addAll(additionalIds);

        ColorIDSet fullColorIDSet = new ColorIDSet(allIDs);
        ColorIDSet partialColorIDSet = new ColorIDSet(ids);

        ColorIDSet currentSet = null;

        int idx = -1;
        boolean fullMatch = false;

        idx = currentAvailableSets.indexOf(fullColorIDSet);

        if (idx != -1) {
            fullMatch = true;
        } else if (idx == -1 && !additionalIds.isEmpty()) {
            idx = currentAvailableSets.indexOf(partialColorIDSet);
        }

        if (idx != -1 && fullMatch) {
            currentSet = currentAvailableSets.get(idx);
        } else if (idx != -1) {
            currentSet = currentAvailableSets.get(idx).extendSet(additionalIds);
        } else {
            currentSet = new ColorIDSet(allIDs);
        }

        if (!fullMatch)
            currentAvailableSets.add(currentSet);

        currentSet.resetTimestamp();
        save();

        return currentSet;
    }

    /**
     *
     * @return numbers of sets registered
     */
    public synchronized int size() {
        return currentAvailableSets.size();
    }

    /**
     * Changes the <code>colorID</code> and <code>favoriteColorID</code> of a
     * user in a set.
     *
     * @param set
     * @param id
     * @param colorID
     * @param favoriteColorID
     * @throws IllegalArgumentException
     *             when the caller tries to set a color multiple times or when
     *             he tries to add a new user.
     */
    public synchronized void updateColor(ColorIDSet set, String id,
                                         int colorID, int favoriteColorID) throws IllegalArgumentException {
        set.setColor(id, colorID);
        set.setFavoriteColor(id, favoriteColorID);
        set.resetTimestamp();
        save();
    }

    @Override
    public String toString() {
        return Arrays.toString(currentAvailableSets.toArray());
    }

    /**
     * Removes entries that are older than the given lifespan.
     *
     * @param lifespan
     *            in milliseconds
     */
    synchronized void remove(long lifespan) {
        long threshold = System.currentTimeMillis() - lifespan;

        for (Iterator<ColorIDSet> it = currentAvailableSets.iterator(); it
                .hasNext();) {
            ColorIDSet colorIdSet = it.next();

            if (colorIdSet.getTimestamp() <= threshold)
                it.remove();
        }

        save();
    }

    /**
     * Saves the current used sets to the preference store.
     */
    private synchronized void save() {

        String serializedData = null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(currentAvailableSets);
            oos.flush();
            serializedData = new String(out.toByteArray(), CHARSET);
        } catch (Exception e) {
            LOG.error("error while saving color sets", e);
            return;
        }

        preferenceStore.setValue(PREFERENCE_STORE_KEY, serializedData);
    }

    /**
     * Loads the last used sets from the preference store.
     */
    @SuppressWarnings("unchecked")
    private void load() {

        currentAvailableSets = new ArrayList<ColorIDSet>();

        String serializedData = preferenceStore.getString(PREFERENCE_STORE_KEY);

        if (serializedData.isEmpty())
            return;

        try {
            currentAvailableSets = (List<ColorIDSet>) new ObjectInputStream(
                    new ByteArrayInputStream(serializedData.getBytes(CHARSET)))
                    .readObject();
        } catch (Exception e) {
            LOG.error("error while loading color sets", e);
        }
    }
}
