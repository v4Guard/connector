package io.v4guard.connector.common.accounts;


import com.fasterxml.jackson.databind.node.ObjectNode;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.accounts.auth.Authentication;

import java.util.logging.Level;

public class AccountShieldSender {

    private final CoreInstance coreInstance;

    public AccountShieldSender(CoreInstance coreInstance) {
        this.coreInstance = coreInstance;
    }

    public void sendSocketMessage(Authentication auth) {
        if (!coreInstance.getRemoteConnection().isReady()) return;

        boolean shieldEnabled = coreInstance.getActiveSettings().getActiveAddons().get("accshield");

        if (!shieldEnabled) return;

        coreInstance.getRemoteConnection().send("accshield:login", coreInstance.getObjectMapper().convertValue(auth, ObjectNode.class));
    }

}
