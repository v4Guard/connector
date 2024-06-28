package io.v4guard.connector.common.compatibility;

public class PlayerFetchResult<PC> {

    private final PC player;
    private final String serverName;
    private final boolean isOnline;

    public PlayerFetchResult(PC player, String serverName, boolean isOnline) {
        this.player = player;
        this.isOnline = isOnline;
        this.serverName = serverName;
    }

    public PC getPlayer() {
        return player;
    }
    public String getServerName() {
        return serverName;
    }

    public boolean isOnline() {
        return isOnline;
    }


}
