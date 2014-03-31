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

package de.fu_berlin.inf.dpp.core.invitation.hooks;

import java.util.Map;

/**
 * A SessionNegotiationHook is a component that whishes to take part in the
 * SessionNegotiation. On the client side, this might be in the form of whishes
 * (during the {@link IncomingSessionNegotiation}), which may then be considered
 * by the host (during the {@link OutgoingSessionNegotiation}). The settings
 * determined by the host need to be applied on the client side.
 * <p/>
 * Hooks are maintained by the {@link SessionNegotiationHookManager}. Hooks may
 * not rely on other hooks, i.e. there are no warranties concerning the order of
 * execution.
 */
public interface ISessionNegotiationHook
{
    /**
     * Retrieves the hook's identifier.
     * <p/>
     * The identifier is used to match the two parts of the hook (on client and
     * host side). Therefore, changing the identifier of a hook breaks the
     * compatibility with older Saros versions.
     *
     * @return A unique string identifying the hook.
     */
    public String getIdentifier();

    /**
     * Receive the client's preferences for later consideration.
     * <p/>
     * During the invitation this method will be called on the <b>client</b>
     * side (see {@link IncomingSessionNegotiation}). The client may use this
     * oppurtunity to tell the host (inviter) his preferences concerning the
     * session parameters.
     *
     * @return The settings in form of [Key, Value] pairs. If <code>null</code>,
     *         the settings won't be transferred to the host.
     */
    public Map<String, String> tellClientPreferences();

    /**
     * Consider the client's preferences on the host side.
     * <p/>
     * This method will be called by the <b>host</b> (during the
     * {@link OutgoingSessionNegotiation}) to determine the session settings.
     * Therefore, the host might consider the preferences of the client.
     *
     * @param input The preferences the client sent during his
     *              {@link IncomingSessionNegotiation} (i.e. the return value of
     *              {@link ISessionNegotiationHook#tellClientPreferences()}).
     * @return The settings determined by the host which -- if not null -- will
     *         be sent back to the client. It's up to the specific hook to which
     *         extent the host considers the wishes of the client.
     */
    public Map<String, String> considerClientPreferences(
            Map<String, String> input);

    /**
     * Duty of the client: Apply the parameters defined by the host.
     * <p/>
     * This method will be called on the <b>client</b>'s side upon reception of
     * the actual session parameters determined by the host. The hook itself is
     * responsible for accessing and modifying the according components (e.g.
     * the {@link SarosSessionManager}). This method will be called right before
     * the Session is created on the client side (via
     * <code>SarosSessionManager.joinSession()</code>) and may not rely on the
     * effects of other hooks.
     *
     * @param settings The parameters concerning the hook at hand which were
     *                 determined by the host during his
     *                 {@link OutgoingSessionNegotiation}.
     */
    public void applyActualParameters(Map<String, String> settings);
}