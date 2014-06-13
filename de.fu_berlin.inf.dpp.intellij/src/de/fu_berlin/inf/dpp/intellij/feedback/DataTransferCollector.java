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

package de.fu_berlin.inf.dpp.intellij.feedback;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.feedback.AbstractStatisticCollector;
import de.fu_berlin.inf.dpp.core.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.ConnectionMode;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.ITransferListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Collects information about the amount of data transfered with the different
 * {@link ConnectionMode}s
 *
 * @author Christopher Oezbek
 */
@Component(module = "feedback")
public class DataTransferCollector extends AbstractStatisticCollector {

    // we currently do not distinguish between sent and received data
    private static class TransferStatisticHolder {
        private long bytesTransferred;
        private long transferTime; // ms
        private int count;
    }

    private final Map<ConnectionMode, TransferStatisticHolder> statistic = new EnumMap<ConnectionMode, TransferStatisticHolder>(
            ConnectionMode.class);

    private final IConnectionManager connectionManager;

    private final ITransferListener dataTransferlistener = new ITransferListener() {

        @Override
        public void sent(final ConnectionMode mode, final long sizeCompressed,
                         final long sizeUncompressed, final long duration) {
            // see processGatheredData
            synchronized (DataTransferCollector.this) {
                TransferStatisticHolder holder = statistic.get(mode);

                if (holder == null) {
                    holder = new TransferStatisticHolder();
                    statistic.put(mode, holder);
                }

                holder.bytesTransferred += sizeCompressed;
                holder.transferTime += sizeUncompressed;
                holder.count++;

                // TODO how to handle overflow ?
            }
        }

        @Override
        public void received(final ConnectionMode mode,
                             final long sizeCompressed, final long sizeUncompressed,
                             final long duration) {
            // TODO differentiate the traffic
            sent(mode, sizeCompressed, sizeUncompressed, duration);
        }

    };

    public DataTransferCollector(StatisticManager statisticManager,
                                 ISarosSession session, IConnectionManager connectionManager) {
        super(statisticManager, session);
        this.connectionManager = connectionManager;
    }

    @Override
    protected synchronized void processGatheredData() {

        for (final Entry<ConnectionMode, TransferStatisticHolder> entry : statistic
                .entrySet()) {
            final ConnectionMode mode = entry.getKey();
            final TransferStatisticHolder holder = entry.getValue();

            data.setTransferStatistic(
                    mode.toString(),
                    holder.count,
                    holder.bytesTransferred / 1024,
                    holder.transferTime,
                    holder.bytesTransferred * 1000.0 / 1024.0
                            / Math.max(1.0, holder.transferTime));

        }
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        connectionManager.addTransferListener(dataTransferlistener);
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        connectionManager.removeTransferListener(dataTransferlistener);
    }
}