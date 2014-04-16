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

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.feedback.AbstractStatisticCollector;
import de.fu_berlin.inf.dpp.core.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;

import java.util.EnumMap;
import java.util.Map;

/**
 * Collects information about the amount of data transfered with the different
 * {@link NetTransferMode}s
 *
 * @author Christopher Oezbek
 */
@Component(module = "feedback")
public class DataTransferCollector extends AbstractStatisticCollector
{

    // we currently do not distinguish between sent and received data
    private static class TransferStatisticHolder {
        private long bytesTransferred;
        private long transferTime; // ms
        private int count;
    }

    private final Map<NetTransferMode, TransferStatisticHolder> statistic = new EnumMap<NetTransferMode, TransferStatisticHolder>(
            NetTransferMode.class);

    private final DataTransferManager dataTransferManager;

    private final ITransferModeListener dataTransferlistener = new ITransferModeListener() {

        @Override
        public void transferFinished(JID jid, NetTransferMode mode,
                boolean incoming, long sizeTransferred, long sizeUncompressed,
                long transmissionMillisecs) {

            // see processGatheredData
            synchronized (DataTransferCollector.this) {
                TransferStatisticHolder holder = statistic.get(mode);

                if (holder == null) {
                    holder = new TransferStatisticHolder();
                    statistic.put(mode, holder);
                }

                holder.bytesTransferred += sizeTransferred;
                holder.transferTime += transmissionMillisecs;
                holder.count++;

                // TODO how to handle overflow ?
            }
        }

        @Override
        public void transferModeChanged(JID jid, NetTransferMode mode) {
            // do nothing
        }
    };

    public DataTransferCollector(StatisticManager statisticManager,
            ISarosSession session, DataTransferManager dataTransferManager) {
        super(statisticManager, session);
        this.dataTransferManager = dataTransferManager;
    }

    @Override
    protected synchronized void processGatheredData() {

        for (final Map.Entry<NetTransferMode, TransferStatisticHolder> entry : statistic
                .entrySet()) {
            final NetTransferMode mode = entry.getKey();
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
        dataTransferManager.addTransferModeListener(dataTransferlistener);
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        dataTransferManager.removeTransferModeListener(dataTransferlistener);
    }
}
