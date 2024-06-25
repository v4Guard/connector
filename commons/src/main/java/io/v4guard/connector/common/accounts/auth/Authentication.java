package io.v4guard.connector.common.accounts.auth;

import java.util.UUID;

public class Authentication {

    private final String username;

    private final UUID uuid;
    private final AuthType authType;
    private final boolean hasPermission;

    public Authentication(String username, UUID uuid, AuthType authType, boolean hasPermission) {
        this.username = username;
        this.uuid = uuid;
        this.authType = authType;
        this.hasPermission = hasPermission;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public AuthType getAuthType() {
        return authType;
    }

}