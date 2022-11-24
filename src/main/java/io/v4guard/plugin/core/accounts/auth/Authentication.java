package io.v4guard.plugin.core.accounts.auth;

import org.bson.Document;

public class Authentication {

    private final String username;
    private final AuthType authType;
    private boolean hasPermission;

    public Authentication(String username, AuthType authType, boolean hasPermission) {
        this.username = username;
        this.authType = authType;
        this.hasPermission = hasPermission;
    }

    public String getUsername() {
        return username;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public Document serialize(){
        return new Document("username", username)
                .append("authType", authType.toString())
                .append("hasPermission", hasPermission);
    }

    public static Authentication deserialize(Document doc){
        return new Authentication(doc.getString("username"), AuthType.valueOf(doc.getString("authType")), doc.getBoolean("hasPermission"));
    }
}
