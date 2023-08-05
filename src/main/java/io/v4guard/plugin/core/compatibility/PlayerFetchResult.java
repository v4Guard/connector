package io.v4guard.plugin.core.compatibility;

public class PlayerFetchResult<PC> {

    private final PC player;
    private final boolean isOnline;

    public PlayerFetchResult(PC player, boolean isOnline) {
        this.player = player;
        this.isOnline = isOnline;
    }

    public PC getPlayer() {
        return player;
    }

    public boolean isOnline() {
        return isOnline;
    }


}
