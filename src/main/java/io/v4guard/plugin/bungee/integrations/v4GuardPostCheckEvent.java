package io.v4guard.plugin.bungee.integrations;

import io.v4guard.plugin.core.check.common.BlockReason;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class v4GuardPostCheckEvent extends Event implements Cancellable {

    private final BlockReason reason;
    private final String username;
    private boolean cancelled = false;

    public v4GuardPostCheckEvent(String username, BlockReason reason) {
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
