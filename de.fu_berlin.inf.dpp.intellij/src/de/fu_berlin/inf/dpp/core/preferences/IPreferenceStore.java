package de.fu_berlin.inf.dpp.core.preferences;



import java.io.IOException;


public interface IPreferenceStore
{
    void flush() throws IOException;

    byte[] getByteArray(String key, byte[] value) ;

    boolean getBoolean(String key, boolean value);

    String getString(String key, String value);

    int getInt(String value);

    boolean getBoolean(String key);

    String getString(String key);

    void putBoolean(String key, boolean value);

    void putByteArray(String key, byte[] value);

    void putString(String key, String value);

}
