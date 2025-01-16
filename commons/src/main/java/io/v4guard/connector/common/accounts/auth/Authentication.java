package io.v4guard.connector.common.accounts.auth;

import java.util.UUID;

public class Authentication {

    private String username;
    private UUID uuid;
    private AuthType authType;
    private boolean hasPermission;

    public Authentication(String username, UUID uuid, AuthType authType, boolean hasPermission) {
        this.username = username;
        this.uuid = uuid;
        this.authType = authType;
        this.hasPermission = hasPermission;
    }

    public Authentication() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public boolean hasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

}