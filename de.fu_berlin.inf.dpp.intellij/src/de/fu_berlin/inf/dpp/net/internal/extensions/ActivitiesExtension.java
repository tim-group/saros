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

package de.fu_berlin.inf.dpp.net.internal.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.*;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.*;
import de.fu_berlin.inf.dpp.net.JID;

import java.util.List;

@XStreamAlias("ADOS")
public class ActivitiesExtension extends SarosSessionPacketExtension
{

    public static final Provider PROVIDER = new Provider();

    @XStreamImplicit
    private final List<IActivityDataObject> activityDataObjects;

    @XStreamAlias("seq")
    @XStreamAsAttribute
    private final int sequenceNumber;

    /**
     * Creates an object that can be transformed into a
     * {@linkplain PacketExtension} using the provider of this extension. All
     * object parameters <b>must be</b> not <code>null</code>.
     *
     * @param sessionID           the session id the {@linkplain IActivityDataObject activity
     *                            data objects} belong to
     * @param activityDataObjects the {@linkplain IActivityDataObject activity data objects}
     *                            that should be included in this extension
     * @param sequenceNumber      the sequence number of the <b>first</b>
     *                            {@linkplain IActivityDataObject activity data object}
     * @Note This constructor does not check for correctness of the input
     * parameters.
     */
    public ActivitiesExtension(String sessionID,
            List<IActivityDataObject> activityDataObjects, int sequenceNumber)
    {
        super(sessionID);
        this.activityDataObjects = activityDataObjects;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Returns the {@linkplain IActivityDataObject activity data objects}
     * included in this extension.
     *
     * @return
     */
    public List<IActivityDataObject> getActivityDataObjects()
    {
        return activityDataObjects;
    }

    /**
     * Returns the sequence number of the first {@linkplain IActivityDataObject
     * activity data object} in the list returned from
     * {@link #getActivityDataObjects}.
     * <p/>
     * <p>
     * All other sequence numbers can be recalculated where the sequence number
     * of the activity data object from
     * <code>getActivityDataObjects().get(x)</code> is
     * <code>getSequenceNumber() + x</code>.
     * </p>
     *
     * @return
     */
    public int getSequenceNumber()
    {
        return sequenceNumber;
    }

    /**
     * @JTourBusStop 7, Activity creation, IActivityDataObject registration:
     * <p/>
     * All IActivityDataObject implementations should be
     * registered with the XStream extensions provider, otherwise
     * annotations like XStreamAlias will not be honored.
     */

    public static class Provider extends
            SarosSessionPacketExtension.Provider<ActivitiesExtension>
    {
        private Provider()
        {
            super("ados", ActivitiesExtension.class,

                    // Misc
                    JID.class,

                    // Jupiter classes

                    DeleteOperation.class,

                    InsertOperation.class,

                    JupiterVectorTime.class,

                    NoOperation.class,

                    SplitOperation.class,

                    TimestampOperation.class,

                    // SPATH
                    SPathDataObject.class,

                    // TODO check XStream doc if those two classes are really needed
                    AbstractActivityDataObject.class,

                    AbstractProjectActivityDataObject.class,

                    // Business ADOs
                    ChangeColorActivityDataObject.class,

                    ChecksumActivityDataObject.class,

                    ChecksumErrorActivityDataObject.class,

                    EditorActivityDataObject.class,

                    FileActivityDataObject.class,

                    FolderActivityDataObject.class,

                    JupiterActivityDataObject.class,

                    NOPActivityDataObject.class,

                    PermissionActivityDataObject.class,

                    ProgressActivityDataObject.class,

                    RecoveryFileActivityDataObject.class,

                    ShareConsoleActivityDataObject.class,

                    StartFollowingActivityDataObject.class,

                    StopActivityDataObject.class,

                    StopFollowingActivityDataObject.class,

                    TextSelectionActivityDataObject.class,

                    VCSActivityDataObject.class,

                    ViewportActivityDataObject.class);
        }
    }
}
