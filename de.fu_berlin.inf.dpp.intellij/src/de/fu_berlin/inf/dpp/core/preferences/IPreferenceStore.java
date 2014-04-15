package de.fu_berlin.inf.dpp.core.preferences;

import de.fu_berlin.inf.dpp.core.exceptions.StorageException;
import de.fu_berlin.inf.dpp.intellij.editor.intl.events.IPropertyChangeListener;

import java.io.IOException;


public interface IPreferenceStore
{
    void flush() throws IOException;

    byte[] getByteArray(String key, byte[] value) throws StorageException;

    boolean getBoolean(String key, boolean value) throws StorageException;

    boolean getBoolean(String key);

    String getString(String key);

    void putBoolean(String key, boolean value, boolean arg2)
            throws StorageException;

    void putByteArray(String key, byte[] value, boolean arg2)
            throws StorageException;

    int getInt(String value);

    void setValue(Object key, boolean value);

    void setValue(Object key, String value);

    String getDefaultString(String key);

    void addPropertyChangeListener(IPropertyChangeListener listener);

    void removePropertyChangeListener(IPropertyChangeListener listener);

}
