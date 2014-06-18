package de.fu_berlin.inf.dpp.core.preferences;



import java.io.IOException;


public interface IPreferenceStore
{
    void flush() throws IOException;

    byte[] getByteArray(String key, byte[] value) ;

    boolean getBoolean(String key, boolean value);

    boolean getBoolean(String key);

    String getString(String key);

    void putBoolean(String key, boolean value, boolean arg2);


    void putByteArray(String key, byte[] value, boolean arg2);

    int getInt(String value);

    void setValue(Object key, boolean value);

    void setValue(Object key, String value);

    String getDefaultString(String key);



}
