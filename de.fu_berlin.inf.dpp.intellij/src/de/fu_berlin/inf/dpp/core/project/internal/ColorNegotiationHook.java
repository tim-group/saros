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

package de.fu_berlin.inf.dpp.core.project.internal;

import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.invitation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

import java.util.HashMap;
import java.util.Map;

public class ColorNegotiationHook implements ISessionNegotiationHook
{
    private static final String HOOK_IDENTIFIER = "colorManagement";
    // This high visibility is not needed for good. Currently there is a color
    // related HACK in the SessionNegotation that relies on these constants.
    public static final String KEY_CLIENT_COLOR = "clientColor";
    public static final String KEY_CLIENT_FAV_COLOR = "clientFavoriteColor";
    public static final String KEY_HOST_COLOR = "hostColor";
    public static final String KEY_HOST_FAV_COLOR = "hostFavoriteColor";

    private PreferenceUtils preferenceUtils;
    private ISarosSessionManager sessionManager;

    public ColorNegotiationHook(PreferenceUtils utils,
            SessionNegotiationHookManager hooks, ISarosSessionManager sessionManager)
    {
        this.preferenceUtils = utils;
        this.sessionManager = sessionManager;
        hooks.addHook(this);
    }

    @Override
    public Map<String, String> tellClientPreferences()
    {
        String favoriteColor = Integer.toString(preferenceUtils
                .getFavoriteColorID());

        Map<String, String> colorSettings = new HashMap<String, String>();
        colorSettings.put(KEY_CLIENT_COLOR, favoriteColor);
        colorSettings.put(KEY_CLIENT_FAV_COLOR, favoriteColor);

        return colorSettings;
    }

    @Override
    public Map<String, String> considerClientPreferences(JID client,
                                                         Map<String, String> input) {
        String hostColor = Integer.toString(sessionManager.getSarosSession()
                .getLocalUser().getColorID());
        String hostFavoriteColor = Integer.toString(preferenceUtils
                .getFavoriteColorID());

        Map<String, String> defined = new HashMap<String, String>();
        defined.put(KEY_CLIENT_COLOR, input.get(KEY_CLIENT_COLOR));
        defined.put(KEY_CLIENT_FAV_COLOR, input.get(KEY_CLIENT_FAV_COLOR));
        defined.put(KEY_HOST_COLOR, hostColor);
        defined.put(KEY_HOST_FAV_COLOR, hostFavoriteColor);

        return defined;
    }

    @Override
    public void applyActualParameters(Map<String, String> settings)
    {
        // TODO Implement the application of the returned color settings. This
        // is currently done with a HACK in IncomingSessionNegotation (see
        // method initializeSession())
    }

    @Override
    public String getIdentifier()
    {
        return HOOK_IDENTIFIER;
    }

}