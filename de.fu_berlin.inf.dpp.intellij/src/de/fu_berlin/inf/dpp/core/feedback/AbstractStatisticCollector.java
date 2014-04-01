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

package de.fu_berlin.inf.dpp.core.feedback;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.picocontainer.Startable;

/**
 * Abstract base class for a StatisticCollector which registers itself with a
 * StatisticManager and informs the StatisticManager at the end of a session of
 * new data via StatisticManager#processCollectedData(StatisticCollector).
 * 
 * @author Lisa Dohrmann
 */

public abstract class AbstractStatisticCollector implements Startable {

    protected final ISarosSession sarosSession;

    protected StatisticManager statisticManager;
    /**
     * The object that contains the gathered statistical information as simple
     * key/value pairs. It is automatically cleared on every session start and
     * filled on every session end.
     * 
     * @see #processGatheredData()
     */
    protected SessionStatistic data;

    @Override
    public void start() {
        doOnSessionStart(sarosSession);
    }

    @Override
    public void stop() {
        doOnSessionEnd(sarosSession);
        notifyCollectionCompleted();
    }

    /**
     * The constructor that has to be called from all implementing classes. It
     * initializes the {@link SessionStatistic}, registers this collector with
     * the {@link StatisticManager}.
     * 
     * @param statisticManager
     * @param sarosSession
     */
    public AbstractStatisticCollector(StatisticManager statisticManager,
        ISarosSession sarosSession) {
        this.statisticManager = statisticManager;
        this.data = new SessionStatistic();
        this.sarosSession = sarosSession;

        statisticManager.registerCollector(this);
    }

    /**
     * Processes the collected data and then hands it to the
     * {@link StatisticManager}. This method is automatically called on session
     * end.
     */
    protected void notifyCollectionCompleted() {
        processGatheredData();
        statisticManager.addData(this, data);
    }

    /**
     * Helper method that calculates the percentage of the given value from the
     * given total value.
     * 
     * @return value / totalValue * 100
     */
    protected int getPercentage(long value, long totalValue) {
        return (int) Math.round(((double) value / totalValue) * 100);
    }

    /**
     * Processes the gathered data, i.e. everything is stored in the
     * {@link #data} map and is afterwards ready to be fetched by the
     * {@link StatisticManager} <br>
     * <br>
     * NOTE: This method is automatically called by
     * {@link #notifyCollectionCompleted()}. Clients only have to implement the
     * method body.
     * 
     * @post the collected information is written to the {@link #data} map
     */
    protected abstract void processGatheredData();

    /**
     * Clients can add their code here that should be executed on session start. <br>
     * doOnSessionStart(ISarosSession) and
     * {@link #doOnSessionEnd(ISarosSession)} are guaranteed to be called in
     * matching pairs with the same project.
     */
    protected abstract void doOnSessionStart(ISarosSession sarosSession);

    /**
     * Clients can add their code here that should be executed on session end. <br>
     * {@link #doOnSessionStart(ISarosSession)} and
     * doOnSessionEnd(ISarosSession) are guaranteed to be called in matching
     * pairs with the same project.
     */
    protected abstract void doOnSessionEnd(ISarosSession sarosSession);

}