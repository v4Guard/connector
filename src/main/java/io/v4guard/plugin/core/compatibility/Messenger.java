package io.v4guard.plugin.core.compatibility;

public interface Messenger {

    void broadcastWithPermission(String message, String permission);
    void sendMessageTo(String playerName, String message);

}
