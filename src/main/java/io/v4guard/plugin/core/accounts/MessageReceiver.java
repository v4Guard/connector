package io.v4guard.plugin.core.accounts;

import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.UnifiedLogger;
import io.v4guard.plugin.core.accounts.auth.Authentication;
import io.v4guard.plugin.core.constants.SettingsKeys;
import io.v4guard.plugin.core.socket.RemoteSettings;
import org.bson.Document;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.logging.Level;

public abstract class MessageReceiver {


    //public static final String CHANNEL = "v4guard:accountshield";

    protected void processPluginMessage(byte[] bytes) {
        if (!CoreInstance.get().getBackend().isReady()) {
            return;
        }

        if (RemoteSettings.getOrDefault(SettingsKeys.INVALIDATE_CACHE, false)) {
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

        try {
            String data = in.readUTF();
            Document doc = Document.parse(data);
            Authentication auth = Authentication.deserialize(doc);
            CoreInstance.get().getAccountShieldSender().sendSocketMessage(auth);
        } catch (Exception exception) {
            UnifiedLogger.get().log(
                    Level.SEVERE
                    , "An exception has occurred while processing plugin message (data=" + new String(bytes) + ")"
                    , exception
            );
        }
    }

}