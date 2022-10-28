package io.v4guard.plugin.core.accounts;

import io.v4guard.plugin.core.accounts.auth.Authentication;
import io.v4guard.plugin.core.accounts.messaging.MessageReceiver;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

public class AccountShieldManager {

    private MessageReceiver receiver;

    public AccountShieldManager(MessageReceiver receiver) {
        this.receiver = receiver;
    }

    public MessageReceiver getReceiver() {
        return receiver;
    }

    public void sendSocketMessage(Authentication auth){

        Document shieldSettings = (Document) v4GuardCore.getInstance().getBackendConnector().getSettings().getOrDefault("addons", new Document());
        boolean shieldEnabled = (boolean) shieldSettings.getOrDefault("accshield", false);
        if(!shieldEnabled) return;

        Document finalDocument = new Document("username", auth.getUsername())
                .append("type", auth.getAuthType().toString());
        v4GuardCore.getInstance().getBackendConnector().getSocket().emit("accshield:login", finalDocument.toJson());
    }
}
