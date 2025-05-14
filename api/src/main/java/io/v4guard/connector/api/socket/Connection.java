package io.v4guard.connector.api.socket;

public interface Connection {

    /**
     * This method should be used before sending any messages to the socket.
     * @return returns true if the socket is ready to send messages
     */
    boolean isReady();

    /**
     * Send a message to the v4Guard server.
     * @param channel the channel to send the message to
     * @param payload the message to send
     */
    void send(String channel, String payload);

    /**
     * Get the socket status.
     * @return the socket status
     */
    SocketStatus getSocketStatus();

    /**
     * Set the socket status.
     * @param socketStatus the socket status
     */
    void setSocketStatus(SocketStatus socketStatus);
}
