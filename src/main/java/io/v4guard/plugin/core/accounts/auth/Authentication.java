package io.v4guard.plugin.core.accounts.auth;

import org.bson.Document;

import java.util.UUID;

public class Authentication {

    private final String username;

    private final UUID uuid;
    private final AuthType authType;
    private boolean hasPermission;

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

    public Document serialize() {
        return new Document("username", username)
                .append("uuid", uuid.toString())
                .append("authType", authType.toString())
                .append("hasPermission", hasPermission);
    }

    public static Authentication deserialize(Document doc){
        return new Authentication(
                doc.getString("username")
                , UUID.fromString(doc.getString("uuid"))
                , AuthType.valueOf(doc.getString("authType"))
                , doc.getBoolean("hasPermission")
        );
    }
}