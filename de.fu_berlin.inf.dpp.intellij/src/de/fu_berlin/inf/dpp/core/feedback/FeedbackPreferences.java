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

package de.fu_berlin.inf.dpp.core.feedback;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;

import java.util.prefs.Preferences;

public class FeedbackPreferences {

    private static Preferences preferences;

    public static synchronized void setPreferences(Preferences preferences) {
        if (preferences == null)
            throw new NullPointerException("preferences is null");

        FeedbackPreferences.preferences = preferences;
    }

    /**
     * Returns the {@link Preferences preferences} that are currently used by
     * the Feedback component.
     * 
     * @throws IllegalStateException
     *             if no preferences instance is available
     */
    public static synchronized Preferences getPreferences() {
        if (FeedbackPreferences.preferences == null)
            throw new IllegalStateException("preferences are not initialized");

        return FeedbackPreferences.preferences;
    }

    public static void applyDefaults(IPreferenceStore defaultPreferences) {
        if (FeedbackPreferences.preferences == null)
            throw new IllegalStateException("preferences are not initialized");

        final String[] keys = { PreferenceConstants.FEEDBACK_SURVEY_DISABLED,
            PreferenceConstants.FEEDBACK_SURVEY_INTERVAL,
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION,
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM,
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION,
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL };

        for (final String key : keys)
            if (preferences.get(key, null) == null)
                preferences.put(key, defaultPreferences.getDefaultString(key));
    }
}
