package io.v4guard.connector.common.accounts;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.accounts.auth.Authentication;
import io.v4guard.connector.common.constants.SettingsKeys;
import io.v4guard.connector.common.socket.ActiveSettings;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.logging.Level;

public abstract class MessageReceiver {

    protected void processPluginMessage(byte[] bytes) {
        if (!CoreInstance.get().getRemoteConnection().isReady()) {
            return;
        }

        ActiveSettings activeSettings = CoreInstance.get().getActiveSettings();

        if (activeSettings.getGeneralSetting(SettingsKeys.INVALIDATE_CACHE, false)) {
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

        try {
            String data = in.readUTF();

            Authentication auth = CoreInstance.get().getObjectMapper().readValue(data, Authentication.class);
            CoreInstance.get().getAccountShieldSender().sendSocketMessage(auth);
        } catch (Exception ignored) {
            /*
            UnifiedLogger.get().log(
                    Level.SEVERE
                    , "An exception has occurred while processing plugin message (data="
                            + new String(bytes)
                            + ")"
                    , exception
            ); */
        }
    }

}