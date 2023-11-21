package io.v4guard.plugin.core.accounts;

import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.accounts.auth.Authentication;
import io.v4guard.plugin.core.constants.SettingsKeys;
import io.v4guard.plugin.core.socket.RemoteSettings;
import org.bson.Document;

public class AccountShieldSender {

    public void sendSocketMessage(Authentication auth) {
        Document shieldSettings = RemoteSettings.getOrDefault(SettingsKeys.ADDONS, new Document());
        boolean shieldEnabled = (boolean) shieldSettings.getOrDefault("accshield", false);

        if (!shieldEnabled) {
            return;
        }

        Document finalDocument = auth.serialize();
        CoreInstance.get().getBackend().getSocket().emit("accshield:login", finalDocument.toJson());
    }

}
