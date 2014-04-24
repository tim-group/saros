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

package de.fu_berlin.inf.dpp.intellij.ui.actions.core;

import de.fu_berlin.inf.dpp.intellij.ui.actions.*;
import de.fu_berlin.inf.dpp.intellij.ui.actions.events.SarosActionListener;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Action factory
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.21
 * Time: 07.39
 */
public class SarosActionFactory
{
    private static Logger log = Logger.getLogger(SarosActionFactory.class);

    private static Map<String, ISarosAction> registeredActions = new HashMap<String, ISarosAction>();

    private static ConnectServerAction connectServerAction;

    static
    {

        //register all actions
        connectServerAction = new ConnectServerAction();
        registerAction(connectServerAction);
        registerAction(new DisconnectServerAction());
        registerAction(new FollowModeAction());
        registerAction(new LeaveSessionAction());
        registerAction(new NewContactAction());
        registerAction(new OpenChartAction());

        for (NotImplementedAction.actions enAction : NotImplementedAction.actions.values())
        {
            registerAction(new NotImplementedAction(enAction));
        }

    }

    /**
     * @param action
     * @return
     */
    private static ISarosAction registerAction(ISarosAction action)
    {
        ISarosAction oldAction = registeredActions.put(action.getActionName(), action);

        if (oldAction != null)
        {
            throw new IllegalArgumentException("Tried to register action " + action.getClass() + " more than once");
        }

        return action;
    }

    /**
     * @param actionName
     * @param listener
     */
    public void addListener(String actionName, SarosActionListener listener)
    {
        ISarosAction action = getAction(actionName);
        action.addActionListener(listener);
    }

    /**
     * @param actionName
     * @param listener
     */
    public void removeListener(String actionName, SarosActionListener listener)
    {
        ISarosAction action = getAction(actionName);
        action.removeActionListener(listener);
    }

    /**
     * @param actionName
     */
    public void removeAllListeners(String actionName)
    {
        ISarosAction action = getAction(actionName);
        action.removeAllActionListeners();
    }

    /**
     * @param actionName
     * @return
     */
    public static ISarosAction getAction(String actionName)
    {
        ISarosAction action = registeredActions.get(actionName);
        if (action == null)
        {
            throw new IllegalArgumentException("Action " + actionName + " not exist!");
        }

        return action;
    }

    /**
     * @param action
     */
    public static void startAction(ISarosAction action)
    {
       // ThreadUtils.runSafeAsync(log,action);
        action.run();
    }

    /**
     * @param actionName
     */
    public static void startAction(String actionName)
    {
        startAction(getAction(actionName));
    }

    //
    // Specific actions
    //

    public static ConnectServerAction getConnectServerAction()
    {
        return connectServerAction;
    }
}
