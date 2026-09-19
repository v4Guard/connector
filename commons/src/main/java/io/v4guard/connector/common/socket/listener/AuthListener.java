package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.api.socket.SocketStatus;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.UniversalTask;
import io.v4guard.connector.api.constants.ListenersConstants;
import io.v4guard.connector.common.socket.ActiveConnection;
import io.v4guard.connector.common.utils.BannerMessage;
import io.v4guard.connector.common.utils.TimestampUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AuthListener implements Emitter.Listener {

    private final CoreInstance backend;
    private final ActiveConnection remoteActiveConnection;
    public UniversalTask notificationTask;
    private final long MAX_AUTH_CODE_LIFETIME = TimeUnit.MINUTES.toMillis(15);
    private long authCodeGotTimestamp;

    public AuthListener(CoreInstance coreInstance, ActiveConnection remoteActiveConnection) {
        this.backend = coreInstance;
        this.remoteActiveConnection = remoteActiveConnection;
    }

    @Override
    public void call(Object... args) {
        JsonNode doc = backend.readTree(args[0].toString());

        if (doc.get(ListenersConstants.AUTH_CODE) != null) {
            remoteActiveConnection.setAuthCode(doc.get(ListenersConstants.AUTH_CODE).asText());
        }

        remoteActiveConnection.setSocketStatus(SocketStatus.valueOf(doc.get(ListenersConstants.AUTH_STATUS).asText()));

        switch (remoteActiveConnection.getSocketStatus()) {
            case NOT_AUTHENTICATED:
                authCodeGotTimestamp = System.currentTimeMillis();
                notificationTask = backend.getPlugin().schedule(() -> {
                    if (remoteActiveConnection.getSocketStatus() != SocketStatus.NOT_AUTHENTICATED) {
                        notificationTask.cancel();
                        return;
                    }

                    if (TimestampUtils.isExpired(authCodeGotTimestamp, MAX_AUTH_CODE_LIFETIME)) {
                        remoteActiveConnection.reconnect();
                        notificationTask.cancel();
                        return;
                    }

                    BannerMessage.create()
                            .line("This instance is not connected to a company!")
                            .line("We're not processing your checks.")
                            .blank()
                            .line("Use the following link to connect your instance to a company:")
                            .line("» https://dashboard.v4guard.io/link/%s", remoteActiveConnection.getAuthCode())
                            .warning();

                }, 1, 60, TimeUnit.SECONDS);
                break;
            case PRE_AUTHENTICATED:
                try {
                    remoteActiveConnection.setAuthCode(null);
                    remoteActiveConnection.writeSecretKey(doc.get("secret").asText());
                } catch (IOException exception) {
                    UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while saving secret key.", exception);
                } finally {
                    remoteActiveConnection.reconnect();
                }
                break;
            case AUTHENTICATED:
                if (remoteActiveConnection.isReconnected()) {
                    return;
                }

                JsonNode company = backend.readTree(doc.get("company").toString());

                BannerMessage.create()
                        .line("This instance is assigned to %s", company.get("name").asText())
                        .line("Plan: %s", this.toTitle(company.get("plan").asText()))
                        .blank()
                        .line("Manage your settings using the dashboard:")
                        .line("» https://dashboard.v4guard.io/companies/select/" + company.get("uuid").asText())
                        .info();

                remoteActiveConnection.setReconnected(true);
        }
    }


    private String toTitle(String text) {
        String lowerCase = text.toLowerCase();
        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }
}
