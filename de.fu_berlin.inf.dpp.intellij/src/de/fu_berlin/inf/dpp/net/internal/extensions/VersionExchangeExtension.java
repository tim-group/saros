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
import de.fu_berlin.inf.dpp.net.XStreamExtensionProvider;

import java.util.HashMap;
import java.util.Map;

@XStreamAlias(/* VersionExchangeExtension */"VEREX")
public class VersionExchangeExtension
{


    public static final Provider PROVIDER = new Provider();

    @XStreamAlias("data")
    private final Map<String, String> data = new HashMap<String, String>();

    /**
     * Associates the specified value with the specified key.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or <code>null</code> if
     *         there was no mapping for key
     */
    public String set(String key, String value)
    {
        return data.put(key, value);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this
     *         map contains no mapping for the key
     */
    public String get(String key)
    {
        return data.get(key);
    }

    public static class Provider extends
            XStreamExtensionProvider<VersionExchangeExtension>
    {
        public Provider()
        {
            super("de.fu_berlin.inf.dpp", "verex", VersionExchangeExtension.class);
        }
        //        private Provider() {
//            super("verex", VersionExchangeExtension.class);
//        }
    }
}
