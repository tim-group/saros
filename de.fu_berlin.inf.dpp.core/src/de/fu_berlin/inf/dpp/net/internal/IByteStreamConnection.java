package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import de.fu_berlin.inf.dpp.net.ConnectionMode;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * A IConnection is responsible for sending data to a particular user
 */
public interface IByteStreamConnection {

    public JID getPeer();

    public void close();

    public boolean isConnected();

    /**
     * Initializes the byte stream connection. After the initialization is
     * performed the byte stream connection must be able to send and receive
     * data.
     */
    public void initialize();

    /**
     * If this call returns the data has been send successfully, otherwise an
     * IOException is thrown with the reason why the transfer failed.
     * 
     * @param data
     *            The data to be sent.
     * @throws IOException
     *             if the send failed
     * @blocking Send the given data as a blocking operation.
     */
    public void send(TransferDescription data, byte[] content)
        throws IOException;

    /**
     * Returns the connection id of this connection.
     * 
     * @return the connection id or <code>null</code> if the connection has no
     *         id
     */
    public String getConnectionID();

    public ConnectionMode getMode();
}