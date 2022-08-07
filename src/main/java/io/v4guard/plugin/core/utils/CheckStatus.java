package io.v4guard.plugin.core.utils;

public class CheckStatus {

    private final String name;
    private final String reason;
    private boolean blocked;

    public CheckStatus(String name, String reason, boolean blocked) {
        this.name = name;
        this.reason = reason;
        this.blocked = blocked;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getName() {
        return name;
    }

    public String getReason() {
        return reason;
    }
}
