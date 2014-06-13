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

package de.fu_berlin.inf.dpp.core.communication.chart.muc.negotiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.fu_berlin.inf.dpp.core.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.invitation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.apache.log4j.Logger;

import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatPreferences;

import de.fu_berlin.inf.dpp.net.util.XMPPUtils;


/**
 * The MUCNegotiationManager is responsible for transmitting the Communication
 * config of the host to all other participants of the shared project during the
 * Invitation process
 *
 * @author ologa
 * @author bkahlert
 */
/*
 * FIXME This class used to transmit something, but that's not the case anymore.
 * In the meantime it was queried for the current configuration. Finally, it
 * provides an invitation hook. The purpose (and the name) of this class need to
 * be clarified.
 */
public class MUCNegotiationManager {

    private static final Logger log = Logger
            .getLogger(MUCNegotiationManager.class);

    protected IPreferenceStore preferences;

    protected SessionIDObservable sessionID;

    protected String password;

    protected MultiUserChatPreferences sessionPreferences;

    protected XMPPConnectionService connectionService;

    private Random random = new Random();

    private final ISessionNegotiationHook negotiationHook = new ISessionNegotiationHook() {
        private static final String HOOK_IDENTIFIER = "multiUserChat";
        private static final String KEY_SERVICE = "service";
        private static final String KEY_ROOMNAME = "roomName";
        private static final String KEY_PASSWORD = "password";

        @Override
        public Map<String, String> tellClientPreferences() {
            // Nothing to do
            return null;
        }

        @Override
        public Map<String, String> considerClientPreferences(JID client,
                                                             Map<String, String> input) {
            // We don't think about the client's preferences. We are the host,
            // so our settings are settled.
            MultiUserChatPreferences ownPreferences = getOwnPreferences();

            Map<String, String> map = new HashMap<String, String>();
            map.put(KEY_PASSWORD, ownPreferences.getPassword());
            map.put(KEY_ROOMNAME, ownPreferences.getRoomName());
            map.put(KEY_SERVICE, ownPreferences.getService());

            return map;
        }

        @Override
        public void applyActualParameters(Map<String, String> settings) {
            setSessionPreferences(new MultiUserChatPreferences(
                    settings.get(KEY_SERVICE), settings.get(KEY_ROOMNAME),
                    settings.get(KEY_PASSWORD)));
        }

        @Override
        public String getIdentifier() {
            return HOOK_IDENTIFIER;
        }
    };

    public MUCNegotiationManager(SessionIDObservable sessionID,
                                 @Nullable XMPPConnectionService connectionService,
                                 IPreferenceStore preferences, SessionNegotiationHookManager hooks) {
        this.sessionID = sessionID;
        this.connectionService = connectionService;
        this.preferences = preferences;
        this.password = String.valueOf(random.nextInt());

        hooks.addHook(negotiationHook);
    }

    /**
     * Load communication settings from PreferenceStore and generate chat room
     * and chat room password.
     */
    public MultiUserChatPreferences getOwnPreferences() {
        return new MultiUserChatPreferences(getMUCService(), "SAROS"
                + sessionID.getValue(), password);
    }

    /**
     * @return temporarily session preferences
     */
    public MultiUserChatPreferences getSessionPreferences() {
        return sessionPreferences;
    }

    /**
     * Set temporarily communication shared project settings
     *
     * @param remotePreferences
     *            received communication settings
     */
    public void setSessionPreferences(MultiUserChatPreferences remotePreferences) {
        log.debug("Got hosts Communication Config: server "
                + remotePreferences.getService() + " room "
                + remotePreferences.getRoomName() + " pw "
                + remotePreferences.getPassword());

        sessionPreferences = remotePreferences;
    }

    private String getMUCService() {
        String service = null;

        boolean useCustomMUCService = preferences
                .getBoolean(PreferenceConstants.FORCE_CUSTOM_MUC_SERVICE);

        String customMUCService = preferences
                .getString(PreferenceConstants.CUSTOM_MUC_SERVICE);

        if (useCustomMUCService && customMUCService != null
                && !customMUCService.isEmpty())
            return customMUCService;

        if (connectionService != null) {
            Connection connection = connectionService.getConnection();

            if (connection != null)
                service = XMPPUtils.getMultiUserChatService(connection,
                        connection.getServiceName());
        }

        if (service == null)
            service = customMUCService;

        if (service != null && service.isEmpty())
            service = null;

        return service;
    }

}
