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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.invitation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

public class NicknameNegotiationHook implements ISessionNegotiationHook {
    private static final String HOOK_IDENTIFIER = "nicknameManagement";
    // This high visibility is not needed for good. Currently there is a
    // nickname
    // related HACK in the SessionNegotation (out/in) that relies on these
    // constants.

    // FIXME this is HACK number 2, the hooking mechanism is currently not very
    // well designed

    public static final String KEY_CLIENT_NICKNAME = "clientNickname";
    public static final String KEY_HOST_NICKNAME = "hostNickname";

    private final PreferenceUtils preferenceUtils;
    private final ISarosSessionManager sessionManager;

    public NicknameNegotiationHook(PreferenceUtils utils,
                                   SessionNegotiationHookManager hooks, ISarosSessionManager sessionManager) {
        this.preferenceUtils = utils;
        this.sessionManager = sessionManager;
        hooks.addHook(this);
    }

    @Override
    public Map<String, String> tellClientPreferences() {
        return Collections.singletonMap(KEY_CLIENT_NICKNAME,
                preferenceUtils.getSessionNickname());
    }

    @Override
    public Map<String, String> considerClientPreferences(final JID client,
                                                         final Map<String, String> input) {

        final Map<String, String> result = new HashMap<String, String>();

        final ISarosSession session = sessionManager.getSarosSession();

        if (session == null)
            return result;

        /*
         * FIXME if two user joining at the same time the nickname will be the
         * same
         */
        final Set<String> occupiedNicknames = new HashSet<String>();

        for (final User user : session.getUsers())
            occupiedNicknames.add(user.getNickname());

        String nickname = input.get(KEY_CLIENT_NICKNAME);

        if (nickname == null || nickname.isEmpty())
            nickname = client.getBareJID().toString();

        int suffix = 2;

        String nicknameToUse = nickname;

        while (occupiedNicknames.contains(nicknameToUse))
            nicknameToUse = nickname + " (" + (suffix++) + ")";

        assert session.getLocalUser().equals(session.getHost());

        result.put(KEY_HOST_NICKNAME, session.getLocalUser().getNickname());
        result.put(KEY_CLIENT_NICKNAME, nicknameToUse);

        return result;
    }

    @Override
    public void applyActualParameters(Map<String, String> settings) {
        // TODO Implement the application of the returned nickname settings.
        // This
        // is currently done with a HACK in IncomingSessionNegotation (see
        // method initializeSession())
    }

    @Override
    public String getIdentifier() {
        return HOOK_IDENTIFIER;
    }

}
