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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.util.HashMap;
import java.util.Map;

/**
 * Action factory
 */
public class SarosActionFactory {

    private static Map<String, AbstractSarosAction> registeredActions = new HashMap<String, AbstractSarosAction>();

    @Inject
    private static Saros saros;

    @Inject
    private static FollowModeAction followModeAction;

    @Inject
    private static LeaveSessionAction leaveSessionAction;

    static {
        SarosPluginContext.initComponent(new SarosActionFactory());
        registerAction(new ConnectServerAction());
        registerAction(new DisconnectServerAction());
        registerAction(followModeAction);
        registerAction(leaveSessionAction);
        registerAction(new ConsistencyAction());
        registerAction(new NewContactAction());
        registerAction(new OpenChartAction());

        for (NotImplementedAction.actions enAction : NotImplementedAction.actions.values()) {
            registerAction(new NotImplementedAction(enAction));
        }

    }

    private static void registerAction(AbstractSarosAction action) {
        registeredActions.put(action.getActionName(), action);
    }


    /**
     * @param actionName
     * @return
     */
    public static AbstractSarosAction getAction(String actionName) {
        AbstractSarosAction action = registeredActions.get(actionName);
        if (action == null) {
            throw new IllegalArgumentException("Action " + actionName + " not exist!");
        }

        return action;
    }

    public static void startAction(AbstractSarosAction action) {
        action.run();
    }
}
