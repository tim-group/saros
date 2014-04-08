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

package de.fu_berlin.inf.dpp.intellij.core;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;

import java.io.IOException;
import java.util.Properties;

//todo: use IntelliJ native mechanism

/**
 * Saros preferences store
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.18
 * Time: 14.03
 */

public class PreferenceStore extends AbstractStore implements IPreferenceStore
{

    public static final String FILE_NAME = "saros_properties.xml";

    /**
     * @param preferenceMap
     */
    public PreferenceStore(Properties preferenceMap)
    {
        this.preferenceMap = preferenceMap;
    }

    /**
     * @throws IOException
     */
    public PreferenceStore()
    {
        this.preferenceMap = new Properties();
        load();
    }

    @Override
    protected String getFileName()
    {
        return FILE_NAME;
    }


    @Override
    public void putBoolean(String key, boolean value1, boolean value2)
    {
        //todo: what to do with value2 ????
        putBoolean(key, value1);
    }


    @Override
    public void putByteArray(String key, byte[] value, boolean arg2)
    {
        //todo: what is arg2???
        putByteArray(key, value);

    }

    @Override
    public void setValue(Object o, boolean arg)
    {
        preferenceMap.put(o, arg);
    }

    @Override
    public void setValue(Object key, String value)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDefaultString(String key)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected String encode(String text)
    {
        return text;
    }

    @Override
    protected String decode(String text)
    {
        return text;
    }
}
