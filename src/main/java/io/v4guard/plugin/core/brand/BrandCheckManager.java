package io.v4guard.plugin.core.brand;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BrandCheckManager {

    private final Set<UUID> playerCheckedSet;

    public BrandCheckManager() {
        this.playerCheckedSet = new HashSet<>();
    }

    public void addPlayer(UUID playerUUID) {
        playerCheckedSet.add(playerUUID);
    }

    public Boolean isPlayerAlreadyChecked(UUID playerUUID) {
        return playerCheckedSet.contains(playerUUID);
    }

    public void removePlayer(UUID playerUUID) {
        playerCheckedSet.remove(playerUUID);
    }
}
