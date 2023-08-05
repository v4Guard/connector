package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.UnifiedLogger;
import io.v4guard.plugin.core.constants.ListenersConstants;
import io.v4guard.plugin.core.socket.Backend;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.utils.TimestampUtils;
import org.bson.Document;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AuthListener implements Emitter.Listener {
    
    private Backend backend;
    private int notificationTaskId;
    private final long MAX_AUTH_CODE_LIFETIME = TimeUnit.MINUTES.toMillis(15);
    private long authCodeGotTimestamp;

    public AuthListener(Backend backend) {
        this.backend = backend;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());

        backend.setAuthCode(doc.getString(ListenersConstants.AUTH_CODE));
        backend.setSocketStatus(SocketStatus.valueOf(doc.getString(ListenersConstants.AUTH_STATUS)));

        switch (backend.getSocketStatus()) {
            case NOT_AUTHENTICATED:
                authCodeGotTimestamp = System.currentTimeMillis();
                notificationTaskId = CoreInstance.get().getPlugin().schedule(() -> {
                    if (backend.getSocketStatus() == SocketStatus.NOT_AUTHENTICATED) {
                        if (TimestampUtils.isExpired(authCodeGotTimestamp, MAX_AUTH_CODE_LIFETIME)) {
                            backend.reconnect();
                            CoreInstance.get().getPlugin().cancelTask(notificationTaskId);
                            return;
                        }

                        UnifiedLogger.get().log(
                                Level.WARNING,
                                "This instance is not connected with your account. Connect it using this link: https://dashboard.v4guard.io/link/"
                                        + backend.getAuthCode());
                    } else {
                        CoreInstance.get().getPlugin().cancelTask(notificationTaskId);
                    }
                }, 1, 60, TimeUnit.SECONDS);
                break;
            case PRE_AUTHENTICATED:
                try {
                    backend.writeSecretKey(doc.getString("secret"));
                } catch (IOException exception) {
                    UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while saving secret key.", exception);
                } finally {
                    backend.reconnect();
                }
                break;
            case AUTHENTICATED:
                if (backend.isReconnected()) {
                    return;
                }

                Document company = doc.get("company", Document.class);

                UnifiedLogger.get().info("Instance connected using secret key: " + doc.getString("secret"));
                UnifiedLogger.get().info("License assigned to " + company.getString("name") + "/" + company.getString("code"));
                UnifiedLogger.get().info("Plan: " + company.getString("plan"));
                UnifiedLogger.get().info("Manage your settings using the dashboard: https://dashboard.v4guard.io/companies/select/" + company.getString("uuid"));

                backend.setReconnected(true);
        }
    }
}
