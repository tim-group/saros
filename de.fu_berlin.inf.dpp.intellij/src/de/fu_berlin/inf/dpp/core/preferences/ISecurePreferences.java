package de.fu_berlin.inf.dpp.core.preferences;


import java.io.IOException;

public interface ISecurePreferences {
    void flush() throws IOException;

    byte[] getByteArray(String key, byte[] defValue);

    byte[] getByteArray(String key);

    boolean getBoolean(String key, boolean defValue);

    void putBoolean(String key, boolean value, boolean arg2)
            throws IOException;

    void putByteArray(String key, byte[] value, boolean arg2)
            throws IOException;

    public String get(String key, String def);

    String absolutePath();

    public double getDouble(String key, double def);

    public float getFloat(String key, float def);

    public int getInt(String key, int def);

    public long getLong(String key, long def);

    public void remove(String key);

    public boolean isEncrypted(String key);

    public String name();

    void clear();
}
