package io.v4guard.connector.platform.bungee.event;

import io.v4guard.connector.common.check.BlockReason;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class PostCheckEvent extends Event implements Cancellable {

    private final BlockReason reason;
    private final String username;
    private boolean cancelled = false;

    public PostCheckEvent(String username, BlockReason reason) {
        this.username = username;
        this.reason = reason;
    }

    public BlockReason getBlockReason() {
        return reason;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}