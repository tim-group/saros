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

package de.fu_berlin.inf.dpp.core.awareness;

import de.fu_berlin.inf.dpp.core.editor.EditorManager;
import de.fu_berlin.inf.dpp.core.editor.RemoteEditorManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that provides methods to collect and retrieve awareness information
 * for session participants (who is following who, which file is currently
 * opened, etc.)
 * <p/>
 * All methods provided by the interface are <b>not</b> thread safe.
 *
 * @author waldmann
 */
public class AwarenessInformationCollector {
    private static final Logger log = Logger
            .getLogger(AwarenessInformationCollector.class);

    protected EditorManager editorManager;
    protected ProjectNegotiationObservable projectNegotiationObservable;
    protected SarosSessionObservable sarosSession;

    /**
     * Who is following who in the session?
     */
    protected Map<JID, JID> followModes = new ConcurrentHashMap<JID, JID>();

    public AwarenessInformationCollector(SarosSessionObservable sarosSession,
                                         ProjectNegotiationObservable projectNegotiationObservable,
                                         EditorManager editorManager) {

        this.sarosSession = sarosSession;
        this.projectNegotiationObservable = projectNegotiationObservable;
        this.editorManager = editorManager;
    }

    /**
     * Make sure to call this, when a session ends, or when a session starts to
     * avoid having outdated information
     */
    public void flushFollowModes() {
        followModes.clear();
    }

    /**
     * Remember that "user" is following "target" in the currently running
     * session.
     *
     * @param user
     * @param target
     */
    public void setUserFollowing(User user, User target) {
        assert user != null;
        assert !(user.equals(target));

        log.debug("Remembering that User " + user + " is now following "
                + target);

        // forget any old states, in case there are any..
        followModes.remove(user.getJID());

        // remember which user he/she is following
        if (target != null) { // don't save null, this is not necessary
            followModes.put(user.getJID(), target.getJID());
        }
    }

    /**
     * Returns the JID of the user that the given user is following, or null if
     * that user does not follow anyone at the moment, or there is no active
     * session.
     *
     * @param user
     * @return
     */
    public JID getFollowedJID(User user) {
        assert user != null;

        ISarosSession session = sarosSession.getValue();
        // should not be called outside of a running session
        if (session == null)
            return null;
        JID resJID = session.getResourceQualifiedJID(user.getJID());
        return resJID == null ? resJID : followModes.get(resJID);
    }

    /**
     * Returns the followee of the given user, or <code>null</code> if that user
     * does not follow anyone at the moment, or there is no active session.
     *
     * @param user
     * @return
     */
    public User getFollowedUser(User user) {
        assert user != null;

        ISarosSession session = sarosSession.getValue();
        // should not be called outside of a running session
        if (session == null)
            return null;

        JID userJID = session.getResourceQualifiedJID(user.getJID());
        if (userJID == null)
            return null;

        JID followeeJID = followModes.get(userJID);
        if (followeeJID == null)
            return null;

        User followee = session.getUser(followeeJID);
        return followee;
    }

    /**
     * Checks if the currently active editor of the given user is shared. The
     * user can be the local or remote one.
     *
     * @return <code>true</code>, if the active editor of the given user is
     * shared, <code>false</code> otherwise
     */
    public boolean isActiveEditorShared(User user) {
        boolean editorActive = false;

        RemoteEditorManager rem = editorManager.getRemoteEditorManager();
        if (rem != null && user != null) {
            if (user.isLocal() && editorManager.isActiveEditorShared()
                    || rem.isRemoteActiveEditorShared(user)) {
                editorActive = true;
            }
        }
        return editorActive;
    }
}