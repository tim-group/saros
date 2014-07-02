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

package de.fu_berlin.inf.dpp.intellij.store;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * IntelliJ preference store
 */
public class PreferenceStore implements IPreferenceStore {
    private static final Logger LOG = Logger.getLogger(PreferenceStore.class);

    public static final String FILE_NAME = "saros_properties.properties";

    private Properties preferenceMap;

    /**
     * Creates a new preference store form preferenceMap
     *
     * @param preferenceMap
     */
    public PreferenceStore(Properties preferenceMap) {
        this.preferenceMap = preferenceMap;
    }

    /**
     * Creates a new PreferenceStore and loads preferences from {#FILE_NAME}.
     */
    public PreferenceStore() {
        try {
            this.preferenceMap = new Properties();
            load();
        } catch (IOException e) {
            LOG.error("could not load preferences", e);
        }
    }

    /**
     * @throws java.io.IOException
     */
    public void save() throws IOException {
        File propFile = new File(FILE_NAME);
        LOG.info("Saving properties [" + propFile.getAbsolutePath() + "]");


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(propFile);
            preferenceMap.store(fos, "Saros properties");
            fos.flush();
            fos.close();
        } finally {
            if (fos != null) {
                IOUtils.closeQuietly(fos);
            }
        }
    }

    /**
     * Loads properties from file
     *
     * @throws IOException
     */
    public void load() throws IOException {
        File propFile = new File(FILE_NAME);
        LOG.info("Loading properties [" + propFile.getAbsolutePath() + "]");

        if (propFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(propFile);
                preferenceMap.load(fis);
                fis.close();
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
    }

    public byte[] getByteArray(String key, byte[] defValue) {
        String value = getString(key);

        try {
            return value == null ?
                    defValue :
                    Hex.decodeHex(value.toCharArray());
        } catch (DecoderException e) {
            LOG.error("Could not decode value", e);
            return defValue;
        }
    }

    public boolean getBoolean(String key, boolean defValue) {
        String value = getString(key, Boolean.valueOf(defValue).toString());
        return Boolean.parseBoolean(value);
    }

    public boolean getBoolean(String key) {
        String value = getString(key);
        return value == null ? BOOLEAN_DEFAULT_DEFAULT : Boolean.valueOf(value)
                .booleanValue();
    }

    public void setValue(String key, boolean value) {
        setValue(key, Boolean.toString(value));
    }

    public int getInt(String key) {
        String value = getString(key);
        if (value == null) {
            return INT_DEFAULT_DEFAULT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return INT_DEFAULT_DEFAULT;
        }
    }

    public void setValue(String key, int value) {
        setValue(key, Integer.toString(value));
    }

    public String getString(String key) {
        String value = preferenceMap.getProperty(key);
        return value == null ? STRING_DEFAULT_DEFAULT : value;
    }

    public String getString(String key, String defValue) {
        return preferenceMap.getProperty(key, defValue);
    }

    public void setValue(String key, String value) {
        preferenceMap.setProperty(key, value);
    }

    public void setValue(String key, byte[] value) {
        setValue(key, new String(Hex.encodeHex(value)));
    }
}
