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
import de.fu_berlin.inf.dpp.intellij.ui.actions.events.SarosActionListener;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parent class for all Saros actions
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */

public abstract class AbstractSarosAction implements ISarosAction
{
    protected static final Logger log = Logger.getLogger(AbstractSarosAction.class);

    protected Saros saros;
    protected Container guiFrame;

    private List<SarosActionListener> actionListeners = new ArrayList<SarosActionListener>();


    protected AbstractSarosAction()
    {
        this.saros = Saros.instance();
    }

    protected void actionStarted()
    {
        log.info("Action started [" + this.getActionName() + "]");

        final List<SarosActionListener> list = new ArrayList<SarosActionListener>(actionListeners);
        for (SarosActionListener actionListener : list)
        {
            actionListener.actionStarted(this);
        }
    }

    protected void actionFinished()
    {
        log.info("Action finished [" + this.getActionName() + "]");

        final List<SarosActionListener> list = new ArrayList<SarosActionListener>(actionListeners);
        for (SarosActionListener actionListener : list)
        {
            actionListener.actionFinished(this);
        }
    }

    @Override
    public void removeAllActionListeners()
    {
        actionListeners.clear();
    }


    @Override
    public void addActionListener(SarosActionListener actionListener)
    {
        actionListeners.add(actionListener);
    }

    @Override
    public void removeActionListener(SarosActionListener actionListener)
    {
        actionListeners.remove(actionListener);
    }

    public void setGuiFrame(Container guiFrame)
    {
        this.guiFrame = guiFrame;
    }
}
