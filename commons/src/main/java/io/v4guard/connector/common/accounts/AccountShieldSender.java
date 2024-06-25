package io.v4guard.connector.common.accounts;


import io.v4guard.connector.common.accounts.auth.Authentication;
import io.v4guard.connector.common.constants.SettingsKeys;

public class AccountShieldSender {

    public void sendSocketMessage(Authentication auth) {
        /*

        //TODO: REFACTOR THIS
        Document shieldSettings = RemoteSettings.getOrDefault(SettingsKeys.ADDONS, new Document());
        boolean shieldEnabled = (boolean) shieldSettings.getOrDefault("accshield", false);

        if (!shieldEnabled) {
            return;
        }

        Document finalDocument = auth.serialize();
        CoreInstance.get().getBackend().getSocket().emit("accshield:login", finalDocument.toJson());
         */
    }

}
