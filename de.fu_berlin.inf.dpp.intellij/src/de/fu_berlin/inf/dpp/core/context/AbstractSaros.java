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

package de.fu_berlin.inf.dpp.core.context;

import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

import java.util.Random;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.28
 * Time: 11.18
 */

public abstract class AbstractSaros
{

    protected static Logger log = Logger.getLogger(AbstractSaros.class);

    public static Random RANDOM = new Random();

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */

    public static final String SAROS = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$

    /**
     * Default server name
     */
    public static final String SAROS_SERVER = "saros-con.imp.fu-berlin.de";

    /**
     * The name of the XMPP namespace used by SarosEclipse. At the moment it is only
     * used to advertise the SarosEclipse feature in the Service Discovery.
     * <p/>
     * TODO Add version information, so that only compatible versions of SarosEclipse
     * can use each other.
     */
    public final static String NAMESPACE = SAROS;

    /**
     * The name of the resource identifier used by SarosEclipse when connecting to the
     * XMPP server (for instance when logging in as john@doe.com, SarosEclipse will
     * connect using john@doe.com/SarosEclipse)
     * <p/>
     * //todo
     */
    public final static String RESOURCE = "Saros"; //$NON-NLS-1$


    /**
     * Sub-namespace for the server. It is used advertise when a server is
     * active.
     */
    public static final String NAMESPACE_SERVER = NAMESPACE + ".server"; //$NON-NLS-1$


    protected static boolean isInitialized;

    protected static boolean isRunning;

    protected AbstractSaros()
    {
        isRunning = true;
    }

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link de.fu_berlin.inf.dpp.core.context.SarosContext#reinject(Object)} will be well defined.
     */
    public static boolean isInitialized()
    {
        return isInitialized;
    }

    public static boolean isIsRunning()
    {
        return isRunning;
    }

    protected static void checkInitialized()
    {
        if (
            //  plugin == null   || //todo
                !isInitialized())
        {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }
}
