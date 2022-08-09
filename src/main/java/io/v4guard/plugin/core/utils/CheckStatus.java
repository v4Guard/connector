package io.v4guard.plugin.core.utils;

import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.v4GuardCore;

public class CheckStatus {

    private static final long EXPIRATION_TIME = 1000L; // 1 second

    private final long createdAt;
    private final String name;
    private String reason;
    private boolean blocked;

    public CheckStatus(String name, String reason, boolean blocked) {
        this.createdAt = System.currentTimeMillis();
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

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() - this.createdAt > EXPIRATION_TIME;
    }

    public void makeCheckExpire(){
        v4GuardCore.getInstance().getCheckManager().getCheckStatusMap().remove(this.name);
        for(CheckProcessor processor : v4GuardCore.getInstance().getCheckManager().getProcessors()){
            processor.actionOnExpire(this);
        }
    }
}
