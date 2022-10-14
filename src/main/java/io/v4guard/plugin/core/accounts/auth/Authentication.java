package io.v4guard.plugin.core.accounts.auth;

import org.bson.Document;

public class Authentication {

    private final String username;
    private final AuthType authType;

    public Authentication(String username, AuthType authType) {
        this.username = username;
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public Document serialize(){
        return new Document("username", username)
                .append("authType", authType.toString());
    }

    public static Authentication deserialize(Document doc){
        return new Authentication(doc.getString("username"), AuthType.valueOf(doc.getString("authType")));
    }
}
