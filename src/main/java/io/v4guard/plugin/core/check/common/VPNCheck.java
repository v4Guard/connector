package io.v4guard.plugin.core.check.common;

import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.v4GuardCore;

public class VPNCheck {

    private static final long EXPIRATION_TIME = 30000L; // 30 seconds
    private static final long POST_LOGIN_EXPIRATION_TIME = 2000L; // 8 seconds

    private final long createdAt;
    private long postLoginTime = 0L;
    private final String name;
    private final String hostname;
    private String reason;

    private CheckStatus status;

    public VPNCheck(String name, String hostname, String reason, boolean blocked) {
        this.createdAt = System.currentTimeMillis();
        this.name = name;
        this.hostname = hostname;
        this.reason = reason;
        this.status = CheckStatus.WAITING;
    }

    public String getHostname() {
        return hostname;
    }

    public void setStatus(CheckStatus status) {
        this.status = status;
    }


    public CheckStatus getStatus() {
        return status;
    }
    public void setPostLoginTime(long postLoginTime) {
        this.postLoginTime = postLoginTime;
    }

    public long getPostLoginTime() {
        return postLoginTime;
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

    public boolean hasPostExpired() {
        return this.postLoginTime != 0L && System.currentTimeMillis() - this.postLoginTime > POST_LOGIN_EXPIRATION_TIME;
    }

    public void performActionOnExpire(){
        for(CheckProcessor processor : v4GuardCore.getInstance().getCheckManager().getProcessors()){
            processor.onExpire(this);
        }
    }
}
