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

import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.annotations.Component;

import de.fu_berlin.inf.dpp.intellij.editor.IEditorManager;
import de.fu_berlin.inf.dpp.core.editor.IEditorManagerBase;

import de.fu_berlin.inf.dpp.editor.colorstorage.ColorIDSet;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.intellij.editor.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.*;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import java.util.*;

/**
 * This manager is responsible for handling color changes and managing the
 * currently available colors.
 *
 * @author Stefan Rossbach
 */
@Component(module = "core")
public class ChangeColorManager extends AbstractActivityProvider implements
        Startable
{

    private static final Logger log = Logger
            .getLogger(ChangeColorManager.class);

    private final ISarosSession sarosSession;
    private final IEditorManagerBase editorManager;
    private final ColorIDSetStorage colorIDSetStorage;

    private final Map<User, Integer> favoriteUserColors = new LinkedHashMap<User, Integer>();

    /*
     * As it is possible that the same color id is taken during invitation
     * multiple times we must use a counter for each id.
     */

    private final Map<Integer, Integer> usedColorIDs = new HashMap<Integer, Integer>();

    private final AbstractActivityReceiver receiver = new AbstractActivityReceiver()
    {

        @Override
        public void receive(ChangeColorActivity activity)
        {
            handleChangeColorActivity(activity);
        }
    };

    private final ISharedProjectListener sessionListener = new AbstractSharedProjectListener()
    {
        @Override
        public void userJoined(User user)
        {

            List<User> currentUsers = new ArrayList<User>();

            synchronized (ChangeColorManager.this)
            {
                favoriteUserColors.put(user, user.getFavoriteColorID());
                currentUsers.addAll(favoriteUserColors.keySet());
            }

            if (!sarosSession.isHost())
            {
                // just removeAll the color, the host will send the correction
                removeColorIdFromPool(user.getColorID());
                return;
            }

            reassignSessionColorIDs(currentUsers, user, true);
        }

        @Override
        public void userLeft(User user)
        {

            List<User> currentUsers = new ArrayList<User>();

            synchronized (ChangeColorManager.this)
            {
                favoriteUserColors.remove(user);
                currentUsers.addAll(favoriteUserColors.keySet());
            }

            if (!sarosSession.isHost())
            {
                addColorIdToPool(user.getColorID());
                return;
            }

            reassignSessionColorIDs(currentUsers, user, false);
        }
    };

    public ChangeColorManager(ISarosSession sarosSession,
            IEditorManager editorManager, ColorIDSetStorage colorIDSetStorage)
    {
        this.sarosSession = sarosSession;
        this.editorManager = editorManager;
        this.colorIDSetStorage = colorIDSetStorage;
    }

    @Override
    public void exec(IActivity activity)
    {
        activity.dispatch(receiver);
    }

    @Override
    public synchronized void start()
    {

        /*
         * If the host does not know its color ID, just assign him one
         */
        if (sarosSession.isHost())
        {
            int colorID = sarosSession.getLocalUser().getColorID();

            favoriteUserColors.put(sarosSession.getLocalUser(), sarosSession
                    .getLocalUser().getFavoriteColorID());

            if (!isValidColorID(colorID))
            {
                colorID = getNextAvailableColorID();
                sarosSession.getLocalUser().setColorID(colorID);
            }
            else
            {
                removeColorIdFromPool(colorID);
            }
        }
        else
        {
            /*
             * Just removeAll the (maybe not valid) color ids, the host will
             * correct it later
             */
            for (User user : sarosSession.getUsers())
            {
                favoriteUserColors.put(user, user.getFavoriteColorID());
                removeColorIdFromPool(user.getColorID());
            }

        }

        installProvider(sarosSession);
        sarosSession.addListener(sessionListener);
    }

    @Override
    public synchronized void stop()
    {
        uninstallProvider(sarosSession);
        sarosSession.removeListener(sessionListener);
    }

    /**
     * Returns a snapshot of the currently in use color IDs.
     *
     * @return
     */
    public synchronized Set<Integer> getUsedColorIDs()
    {
        return new HashSet<Integer>(usedColorIDs.keySet());
    }

    /**
     * Changes the color id for the current local user. The change is done
     * asynchronously and may not be available immediately. Negative color id
     * values will result in the next available color id.
     *
     * @param colorID the new color ID for the current session
     */
    public void changeColorID(int colorID)
    {
        fireActivity(new ChangeColorActivity(sarosSession.getLocalUser(),
                sarosSession.getHost(), sarosSession.getLocalUser(), colorID));
    }

    private void handleChangeColorActivity(ChangeColorActivity activity)
    {

        boolean fireChanges = false;
        User source = activity.getSource();
        User affected = activity.getAffected();
        int colorID = activity.getColorID();

        List<User> currentUsers = new ArrayList<User>();

        synchronized (this)
        {
            currentUsers.addAll(favoriteUserColors.keySet());

            if (affected == null)
            {
                log.warn("received color id change for a user that is no longer part of the session");
                return;
            }

            log.debug("received color id change fo user : " + affected + " ["
                    + activity.getColorID() + "]");

            // host send us an update for a user
            if (source.isHost() && !sarosSession.isHost())
            {

                addColorIdToPool(affected.getColorID());
                removeColorIdFromPool(colorID);

                // this fails if a new copy is returned !
                affected.setColorID(colorID);
            }
            else
            {

                assert sarosSession.isHost() : "only the session host can assign a color id";

                if (!isColorIDAvailable(colorID))
                {
                    colorID = getNextAvailableColorID();
                }
                else
                {
                    removeColorIdFromPool(colorID);
                }

                addColorIdToPool(affected.getColorID());

                affected.setColorID(colorID);
                fireChanges = true;
            }
        }

        if (fireChanges)
        {
            broadcastColorIDChange(affected, currentUsers,
                    affected.getColorID());
        }

      //  updateColorSet(currentUsers);

        editorManager.colorChanged();
        editorManager.refreshAnnotations();
    }

    private Collection<String> asIDCollection(Collection<User> users) {
        List<String> result = new ArrayList<String>(users.size());

        for (User user : users)
            result.add(user.getJID().getBareJID().toString());

        return result;
    }

    /*
     * original algorithm by: fzieris and pschlott, modified and integrated by
     * srossbach
     *
     * it ensures one invariant: favorite colors are optimally distributed
     *
     * it assumes following invariants: if a colorIdSet exists, it contains no
     * color collisions (except when a colorIdSet is created for the first time,
     * then all colors are UserColorId.UNKNOWN)
     */
    private void reassignSessionColorIDs(List<User> currentUsers, User user,
                                         boolean joined) {

        if(true)
            return;


        assert sarosSession.isHost() : "only the session host can assign a color id";

        log.debug("reassigning color IDs for the current session users");

        synchronized (this) {

            /*
             * release all colors, see join / left, the host must handle the
             * join / left of a user here !
             */
            currentUsers.remove(user);

            // we need to release all current colors
            for (User currentUser : currentUsers)
                addColorIdToPool(currentUser.getColorID());

            /*
             * if a user just joined, he must be in the current users list. if
             * he left, his color became available in the pool
             */
            if (joined)
                currentUsers.add(user);
            else
                addColorIdToPool(user.getColorID());

            Map<User, Integer> assignedColors = assumeNoFavoriteColorCollisions(currentUsers);

            assert assignedColors.size() == currentUsers.size();

            ColorIDSet colorIDSet = colorIDSetStorage
                    .getColorIDSet(asIDCollection(currentUsers));

            resolveColorConflicts: {

                // no conflict = OK
                if (isOptimalColorAssignment(assignedColors)) {
                    log.debug("color conflict resolve result = NO CONFLICT");
                    break resolveColorConflicts;
                }

                Map<User, Integer> lastKnownFavoriteColors = getLastKnownFavoriteColors(
                        assignedColors, colorIDSet);

                /*
                 * we already solved the problem in a former session = use the
                 * resolved colors again ... see above assignedColors is
                 * initialized with the current favorite colors
                 */
                if (lastKnownFavoriteColors.equals(assignedColors)) {

                    applyStoredColors(assignedColors, colorIDSet);

                    /*
                     * if there are users with favorite color
                     * UserColorID.UNKNOWN, then the optimal assignment cannot
                     * be known. So if there was already a valid assignment for
                     * these users we choose that, so that the users experience
                     * the reuse of colors.
                     */
                    if (favoriteUserColors.containsValue(UserColorID.UNKNOWN)
                            && isValidColorAssignment(assignedColors)) {
                        log.debug("color conflict resolve result = FAVORITE COLORS UNKNOWN, USING PREVIOUS COLOR ASSIGNMENT");
                        break resolveColorConflicts;
                    }

                    /*
                     * if color assignment is optimal, assignment is resolved.
                     */
                    if (isOptimalColorAssignment(assignedColors)) {
                        log.debug("color conflict resolve result = ALREADY SOLVED");
                        break resolveColorConflicts;
                    } else {
                        // the colorIdSet was not optimal, reassign colors
                        assignedColors = assumeNoFavoriteColorCollisions(currentUsers);
                    }
                }

                // resolve the problem
                autoAssignColors(assignedColors);

                /*
                 * if favorite colors weren't initialized, there cannot be an
                 * optimal assignment
                 */
                if (!favoriteUserColors.containsValue(UserColorID.UNKNOWN))
                    assert isOptimalColorAssignment(assignedColors);

                /* release all colors again as they will be removed again */
                for (int colorID : assignedColors.values())
                    addColorIdToPool(colorID);

                log.debug("color conflict resolve result = RESOLVED");

            } // END resolveColorConflicts

            log.debug("new color assignment: " + assignedColors);

            updateColorAndUserPools(assignedColors);

            updateColorSet(currentUsers);
        }

        for (User currentUser : currentUsers)
            broadcastColorIDChange(currentUser, currentUsers,
                    currentUser.getColorID());

        editorManager.colorChanged();
        editorManager.refreshAnnotations();

    }

    private void updateColorAndUserPools(Map<User, Integer> assignedColors)
    {
        for (Map.Entry<User, Integer> entry : assignedColors.entrySet())
        {
            // make sure a used color is no longer available
            Integer colorId = entry.getValue();
            removeColorIdFromPool(colorId);

            // make sure user uses the colorId we calculated
            User user = entry.getKey();
            user.setColorID(colorId);
        }
    }

    /**
     * Proofs the if the invariant for optimal color assignment holds true. Here
     * we take this invariant to be:
     * <ul>
     * <li>Each favorite color is used in the final assignment</li>
     * <li>The assignment is valid (i.e each color is unique, and none of the
     * colors is UserColorId.UNKNOWN</li>
     * </ul>
     *
     * @param assignedColors
     * @return
     */
    private synchronized boolean isOptimalColorAssignment(
            Map<User, Integer> assignedColors)
    {
        return assignedColors.values().containsAll(
                new HashSet<Integer>(favoriteUserColors.values()))
                && isValidColorAssignment(assignedColors);
    }

    private synchronized boolean isValidColorAssignment(
            Map<User, Integer> assignedColors)
    {
        return isUnique(assignedColors.values())
                && !assignedColors.containsValue(UserColorID.UNKNOWN);
    }

    private synchronized void applyStoredColors(
            Map<User, Integer> assignedColors, ColorIDSet colorIDSet) {

        for (Map.Entry<User, Integer> e : assignedColors.entrySet()) {
            e.setValue(colorIDSet.getColor(e.getKey().getJID().getBareJID()
                    .toString()));
        }

    }

    /**
     * assigns the next available color for the users that couldn't get
     * <ul>
     * <li>their favorite color</li>
     * <li>a color from a previous session</li>
     * </ul>
     *
     * @param assignedColors
     */
    private synchronized void autoAssignColors(Map<User, Integer> assignedColors)
    {
        List<User> usersToAutoAssignColors = new ArrayList<User>();

        for (Map.Entry<User, Integer> entry : assignedColors.entrySet())
        {

            if (!isColorIDAvailable(entry.getValue()))
            {
                usersToAutoAssignColors.add(entry.getKey());
                continue;
            }
            removeColorIdFromPool(entry.getValue());
        }

        for (User currentUser : usersToAutoAssignColors)
        {
            int colorID = getNextAvailableColorID();
            assignedColors.put(currentUser, colorID);
        }
    }

    private synchronized Map<User, Integer> getLastKnownFavoriteColors(
            Map<User, Integer> assignedColors, ColorIDSet colorIDSet) {
        Map<User, Integer> lastKnownFavoriteColors = new LinkedHashMap<User, Integer>();

        for (User currentUser : assignedColors.keySet()) {
            lastKnownFavoriteColors.put(
                    currentUser,
                    colorIDSet.getFavoriteColor(currentUser.getJID().getBareJID()
                            .toString()));
        }
        return lastKnownFavoriteColors;
    }

    /**
     * creates a color assignment, which assumes the favorite colors of the
     * users in the session don't collide
     *
     * @param currentUsers
     * @return
     */
    private synchronized Map<User, Integer> assumeNoFavoriteColorCollisions(
            List<User> currentUsers)
    {

        // assume everyone gets his favorite color
        Map<User, Integer> assignedColors = new LinkedHashMap<User, Integer>();

        /*
         * iterate over favoriteUserColors to ensure no favorite color
         * "stealing" if a new user joins a.k.a assign colors in join order
         */
        for (Map.Entry<User, Integer> entry : favoriteUserColors.entrySet())
        {
            if (currentUsers.contains(entry.getKey()))
            {

                /*
                 * make sure to use the right user object reference (although
                 * they should be the same)
                 */
                assignedColors.put(
                        currentUsers.get(currentUsers.indexOf(entry.getKey())),
                        entry.getValue());
            }
        }

        return assignedColors;
    }

    /**
     * Notify the recipients about the color change of the affected users. If
     * the session host is included in the recipient list it will be ignored.
     */
    private void broadcastColorIDChange(User affected, List<User> recipients,
            int colorID)
    {

        assert sarosSession.isHost() : "only the session host can broadcast color id changes";

        for (User user : recipients)
        {
            if (user.isHost())
            {
                continue;
            }

            fireActivity(new ChangeColorActivity(sarosSession.getLocalUser(),
                    user, affected, colorID));
        }
    }

    private boolean isValidColorID(int colorID)
    {
        if (colorID < 0)
        {
            assert !UserColorID.isValid(colorID) : "negative color id must not be valid";
        }

        return UserColorID.isValid(colorID);
    }

    /**
     * Removes and returns the next available color ID from the color pool.
     */
    private synchronized int getNextAvailableColorID()
    {

        List<Integer> usedColorIDs = new ArrayList<Integer>(getUsedColorIDs());

        Collections.sort(usedColorIDs);

        int unusedColorID = 0;

        for (int usedColorID : usedColorIDs)
        {
            if (!isValidColorID(usedColorID))
            {
                continue;
            }

            if (unusedColorID < usedColorID)
            {
                break;
            }

            unusedColorID = usedColorID + 1;
        }

        removeColorIdFromPool(unusedColorID);
        return unusedColorID;
    }

    private synchronized boolean isColorIDAvailable(int colorID)
    {
        if (!isValidColorID(colorID))
        {
            return false;
        }

        return !usedColorIDs.containsKey(colorID);

    }

    /**
     * Adds the color ID to the color pool. Does nothing if the color ID is
     * invalid.
     */
    private synchronized void addColorIdToPool(int colorID)
    {
        if (!isValidColorID(colorID))
        {
            return;
        }

        Integer colorIDUseCount = usedColorIDs.get(colorID);

        if (colorIDUseCount == null)
        {
            log.warn("color id: " + colorID
                    + " was added although it was never removed");
            colorIDUseCount = 0;
        }
        else
        {
            colorIDUseCount--;
        }

        log.trace("color id: " + colorID + " is currently used "
                + colorIDUseCount + " times");

        /*
         * removeAll the colorID to ensure that getNextAvailableColorID returns
         * correct values
         */
        if (colorIDUseCount == 0)
        {
            usedColorIDs.remove(colorID);
        }
        else
        {
            usedColorIDs.put(colorID, colorIDUseCount);
        }
    }

    /**
     * Removes the color ID from the color pool. Does nothing if the color ID is
     * invalid.
     */
    private synchronized void removeColorIdFromPool(int colorID)
    {
        if (!isValidColorID(colorID))
        {
            return;
        }

        Integer colorIDUseCount = usedColorIDs.get(colorID);

        if (colorIDUseCount == null)
        {
            colorIDUseCount = 0;
        }

        colorIDUseCount++;

        log.trace("color id: " + colorID + " is currently used "
                + colorIDUseCount + " times");

        usedColorIDs.put(colorID, colorIDUseCount);
    }

    private synchronized void updateColorSet(Collection<User> users) {

        ColorIDSet colorIDSet = colorIDSetStorage
                .getColorIDSet(asIDCollection(users));

        log.debug("updating color id set: "
                + Arrays.toString(colorIDSet.getParticipants().toArray()));

        /*
         * reset colors to unknown otherwise we may get an illegal state
         * exception
         */
        for (User user : users)
            colorIDSetStorage.updateColor(colorIDSet, user.getJID()
                            .getBareJID().toString(), UserColorID.UNKNOWN,
                    UserColorID.UNKNOWN);

        for (User user : users) {
            if (!isValidColorID(user.getColorID()))
                continue;

            /*
             * the host still sends color updates so it is ok to abort here and
             * leaving the color set in a dirty state
             */
            if (!colorIDSet.isAvailable(user.getColorID())) {
                assert !sarosSession.isHost() : "invalid color state on host side";
                break;
            }

            log.trace("updating color id set: user '" + user + "' id '"
                    + user.getColorID() + "' fav id '"
                    + favoriteUserColors.get(user) + "'");

            colorIDSetStorage.updateColor(colorIDSet, user.getJID()
                            .getBareJID().toString(), user.getColorID(),
                    favoriteUserColors.get(user));
        }
    }

    private Collection<JID> asJIDCollection(Collection<User> users)
    {
        List<JID> result = new ArrayList<JID>(users.size());

        for (User user : users)
        {
            result.add(user.getJID());
        }

        return result;
    }

    private boolean isUnique(Collection<Integer> collection)
    {
        Set<Integer> set = new HashSet<Integer>(collection.size());

        for (Integer i : collection)
        {
            if (set.contains(i))
            {
                return false;
            }

            set.add(i);
        }

        return true;
    }
}
