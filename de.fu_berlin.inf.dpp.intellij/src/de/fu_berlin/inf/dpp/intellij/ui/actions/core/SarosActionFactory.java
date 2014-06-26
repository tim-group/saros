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

import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.actions.*;
import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Action factory
 */
public class SarosActionFactory
{
    private static Logger LOG = Logger.getLogger(SarosActionFactory.class);

    private static Map<String, ISarosAction> registeredActions = new HashMap<String, ISarosAction>();

    private static ConnectServerAction connectServerAction;

    private static Saros saros = Saros.instance();

    static
    {
        MutablePicoContainer pico = saros.getSarosContext().createSimpleChildContainer();
        //register all actions
        connectServerAction = new ConnectServerAction();
        registerAction(connectServerAction);
        registerAction(new DisconnectServerAction());
        registerAction(pico.getComponent(FollowModeAction.class));
        registerAction(pico.getComponent(LeaveSessionAction.class));
        registerAction(new ConsistencyAction());
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
    private static ISarosAction registerAction(AbstractSarosAction action)
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
        // ThreadUtils.runSafeAsync(LOG,action);
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
