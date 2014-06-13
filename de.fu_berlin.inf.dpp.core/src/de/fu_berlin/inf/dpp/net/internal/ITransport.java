package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.jivesoftware.smack.Connection;

import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * This interface is used to define various transport methods (probably only XEP
 * 65 SOCKS5, XEP 47 in-band bytestream and XEP 16x Jingle.
 */
public interface ITransport {

    /**
     * Delimiter that must be used to encode various arguments into a session
     * id.
     */
    public static final char SESSION_ID_DELIMITER = ':';

    /**
     * Establishes a {@link IByteStreamConnection connection} to the given JID.
     * 
     * @param connectionID
     *            an ID used to identify this connection on the remote side
     * @param peer
     *            a <b>resource qualified</b> JID to connect to
     * @throws NullPointerException
     *             if connectionID or peer is <code>null</code>
     * @throws IllegalArgumentException
     *             if the connection id is an empty string or contains at least
     *             one {@value #SESSION_ID_DELIMITER} character
     * @throws IOException
     *             if no connection could be established
     * @throws InterruptedException
     *             if the connection establishment was interrupted
     */
    public IByteStreamConnection connect(String connectionID, JID peer)
        throws IOException, InterruptedException;

    /**
     * Initializes the transport. After initialization the transport is able to
     * establish connections via {@link #connect}.
     * 
     * @param connection
     * @param listener
     */
    public void initialize(Connection connection,
        IByteStreamConnectionListener listener);

    /**
     * Un-initializes the transport. After un-initialization the transport is
     * not able to establish connections via {@link #connect}.
     */
    public void uninitialize();
}