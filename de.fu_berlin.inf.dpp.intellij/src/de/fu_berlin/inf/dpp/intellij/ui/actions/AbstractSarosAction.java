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

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

/**
 * Parent class for all Saros actions
 */
public abstract class AbstractSarosAction {
    protected static final Logger LOG = Logger.getLogger(AbstractSarosAction.class);

    @Inject
    protected Saros saros;

    private final List<SarosActionListener> actionListeners = new ArrayList<SarosActionListener>();
    private final List<UIRefreshListener> refreshListeners = new ArrayList<UIRefreshListener>();

    protected AbstractSarosAction() {
        SarosPluginContext.initComponent(this);
    }

    protected void actionStarted() {
        for (SarosActionListener actionListener : actionListeners) {
            assert actionListener != null;
            actionListener.actionStarted(this);
        }
    }

    protected void actionFinished() {
        for (SarosActionListener actionListener : actionListeners) {
            assert actionListener != null;
            actionListener.actionFinished(this);
        }
    }

    protected void refreshAll() {
        for (UIRefreshListener refreshListener : refreshListeners) {
            refreshListener.refresh(this);
        }
    }

    public void addActionListener(SarosActionListener actionListener) {
         actionListeners.add(actionListener);
    }

    public void addRefreshListener(UIRefreshListener refreshListener) {
        refreshListeners.add(refreshListener);
    }

    public abstract String getActionName();

    public abstract void execute();
}
