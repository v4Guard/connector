package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.UniversalTask;
import io.v4guard.connector.common.constants.ListenersConstants;
import io.v4guard.connector.common.socket.Connection;
import io.v4guard.connector.common.socket.SocketStatus;
import io.v4guard.connector.common.utils.TimestampUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AuthListener implements Emitter.Listener {

    private final CoreInstance backend;
    private final Connection remoteConnection;
    public UniversalTask notificationTask;
    private final long MAX_AUTH_CODE_LIFETIME = TimeUnit.MINUTES.toMillis(15);
    private long authCodeGotTimestamp;

    public AuthListener(CoreInstance coreInstance, Connection remoteConnection) {
        this.backend = coreInstance;
        this.remoteConnection = remoteConnection;
    }

    @Override
    public void call(Object... args) {
        JsonNode doc = backend.readTree(args[0].toString());

        if (doc.get(ListenersConstants.AUTH_CODE) != null) {
            remoteConnection.setAuthCode(doc.get(ListenersConstants.AUTH_CODE).asText());
        }

        remoteConnection.setSocketStatus(SocketStatus.valueOf(doc.get(ListenersConstants.AUTH_STATUS).asText()));

        switch (remoteConnection.getSocketStatus()) {
            case NOT_AUTHENTICATED:
                authCodeGotTimestamp = System.currentTimeMillis();
                notificationTask = backend.getPlugin().schedule(() -> {
                    if (remoteConnection.getSocketStatus() != SocketStatus.NOT_AUTHENTICATED) {
                        notificationTask.cancel();
                        return;
                    }

                    if (TimestampUtils.isExpired(authCodeGotTimestamp, MAX_AUTH_CODE_LIFETIME)) {
                        remoteConnection.reconnect();
                        notificationTask.cancel();
                        return;
                    }

                    UnifiedLogger.get().log(
                            Level.WARNING,
                            "This instance is not connected with your account. Connect it using this link: https://dashboard.v4guard.io/link/"
                                    + remoteConnection.getAuthCode());

                }, 1, 60, TimeUnit.SECONDS);
                break;
            case PRE_AUTHENTICATED:
                try {
                    remoteConnection.setAuthCode(null);
                    remoteConnection.writeSecretKey(doc.get("secret").asText());
                } catch (IOException exception) {
                    UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while saving secret key.", exception);
                } finally {
                    remoteConnection.reconnect();
                }
                break;
            case AUTHENTICATED:
                if (remoteConnection.isReconnected()) {
                    return;
                }

                JsonNode company = backend.readTree(doc.get("company").toString());

                UnifiedLogger.get().info("Instance connected using secret key: " + doc.get("secret").asText());
                UnifiedLogger.get().info("License assigned to " + company.get("name").asText() + "/" + company.get("code").asText());
                UnifiedLogger.get().info("Plan: " + company.get("plan").asText());
                UnifiedLogger.get().info("Manage your settings using the dashboard: https://dashboard.v4guard.io/companies/select/" + company.get("uuid").asText());

                remoteConnection.setReconnected(true);
        }
    }
}
