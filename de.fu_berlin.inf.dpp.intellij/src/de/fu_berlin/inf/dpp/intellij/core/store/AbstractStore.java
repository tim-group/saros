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

package de.fu_berlin.inf.dpp.intellij.core.store;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Abstract IntelliJ preference store
 */
public abstract class AbstractStore implements IPreferenceStore {
    protected final static Logger LOG = Logger.getLogger(AbstractStore.class);
    protected Properties preferenceMap;

    protected abstract String getFileName();

    protected abstract String encode(String text);

    protected abstract String decode(String text);

    /**
     * @throws java.io.IOException
     */
    public void save() throws IOException {
        File propFile = new File(getFileName());
        LOG.info("Saving properties [" + propFile.getAbsolutePath() + "]");

        FileOutputStream fos = new FileOutputStream(propFile);
        preferenceMap.storeToXML(fos, "Saros properties", "UTF-8");
        fos.flush();
        fos.close();
    }

    /**
     * Loads properties from file
     *
     * @throws IOException
     */
    public void load() throws IOException {
        File propFile = new File(getFileName());
        LOG.info("Loading properties [" + propFile.getAbsolutePath() + "]");

        if (propFile.exists()) {
            FileInputStream fis = new FileInputStream(propFile);
            preferenceMap.loadFromXML(fis);
            fis.close();
        }
    }

    /**
     * @param key
     * @param defValue
     * @return
     */
    public byte[] getByteArray(String key, byte[] defValue) {
        String value = getString(key);

        try {
            return value == null ?
                defValue :
                Hex.decodeHex(value.toCharArray());
        } catch (DecoderException e) {
            LOG.error("Could not decode value", e);

            throw new RuntimeException(e);
        }
    }

    /**
     * @param key
     * @return
     */
    public byte[] getByteArray(String key) {
        return getByteArray(key, null);
    }

    /**
     * @param key
     * @param value
     */
    public void putByteArray(String key, byte[] value) {
        putString(key, new String(Hex.encodeHex(value)));
    }

    /**
     * @param key
     * @param defValue
     * @return
     */
    public boolean getBoolean(String key, boolean defValue) {
        String value = getString(key, Boolean.valueOf(defValue).toString());
        return Boolean.parseBoolean(value);
    }

    /**
     * @param key
     * @return
     */
    public boolean getBoolean(String key) {
        String value = getString(key);
        return value != null && Boolean.parseBoolean(value);
    }

    /**
     * @param key
     * @param value
     */
    public void putBoolean(String key, boolean value) {
        putString(key, Boolean.toString(value));
    }

    /**
     * @param key
     * @return
     */
    public int getInt(String key) {
        String value = getString(key);
        return value == null || value.isEmpty() ? -1 : Integer.parseInt(value);
    }

    /**
     * @param key
     * @param value
     */
    public void putInt(String key, int value) {
        putString(key, Integer.toString(value));
    }

    /**
     * @param key
     * @return
     */
    public String getString(String key) {
        return decode(preferenceMap.getProperty(key));
    }

    /**
     * @param key
     * @param defValue
     * @return
     */
    public String getString(String key, String defValue) {
        return decode(preferenceMap.getProperty(key, defValue));
    }

    /**
     * @param key
     * @param value
     */
    public void putString(String key, String value) {
        preferenceMap.setProperty(key, encode(value));
    }

    /**
     * Removes all data from memory
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        preferenceMap = new Properties();
    }
}
