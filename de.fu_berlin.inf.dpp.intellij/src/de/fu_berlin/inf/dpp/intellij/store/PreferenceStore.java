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

    @Override
    public int getInt(String key) {
        String value = preferenceMap.getProperty(key);
        if (value == null) {
            return INT_DEFAULT_DEFAULT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return INT_DEFAULT_DEFAULT;
        }
    }

    @Override
    public boolean getBoolean(String key) {
        String value = preferenceMap.getProperty(key);
        return value == null ? BOOLEAN_DEFAULT_DEFAULT : Boolean.valueOf(value);
    }

    @Override
    public String getString(String key) {
        String value = preferenceMap.getProperty(key);
        return value == null ? STRING_DEFAULT_DEFAULT : value;
    }

    @Override
    public void setValue(String key, int value) {
        setValue(key, Integer.toString(value));
    }

    @Override
    public void setValue(String key, boolean value) {
        setValue(key, Boolean.toString(value));
    }

    @Override
    public void setValue(String key, String value) {
        preferenceMap.setProperty(key, value);
    }
}
