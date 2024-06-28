package io.v4guard.connector.common.compatibility;

public interface Messenger {

    void broadcastWithPermission(String message, String permission);
    void sendMessageTo(String playerName, String message);

}
