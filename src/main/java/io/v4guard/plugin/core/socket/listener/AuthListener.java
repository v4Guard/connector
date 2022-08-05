package io.v4guard.plugin.core.socket.listener;

import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.socket.client.IO;
import io.socket.emitter.Emitter;
import org.bson.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthListener implements Emitter.Listener {
    
    BackendConnector backendConnector;

    public AuthListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        try {
            backendConnector.setSocketStatus(SocketStatus.valueOf(doc.getString("status")));
            if (backendConnector.getSocketStatus().equals(SocketStatus.NOT_AUTHENTICATED)) {
                v4GuardCore.getInstance().getLogger().log(Level.WARNING,"This instance is not connected with your account. Connect it using this link: https://dashboard.v4guard.io/link/" + doc.getString("code"));
            } else if (backendConnector.getSocketStatus().equals(SocketStatus.PRE_AUTHENTICATED)) {
                File secrets;
                if (!v4GuardCore.getInstance().getDataFolder().exists()) {
                    v4GuardCore.getInstance().getDataFolder().createNewFile();
                }
                if (!(secrets = new File(v4GuardCore.getInstance().getDataFolder(), "vpn.key")).exists()) {
                    secrets.createNewFile();
                    FileWriter writer = new FileWriter(new File(v4GuardCore.getInstance().getDataFolder(), "vpn.key"));
                    writer.write(doc.getString("secret"));
                    writer.close();
                } else {
                    FileWriter writer = new FileWriter(new File(v4GuardCore.getInstance().getDataFolder(), "vpn.key"));
                    writer.write(doc.getString("secret"));
                    writer.close();
                }
                backendConnector.getSocket().disconnect();
                backendConnector.getOptions().auth = Collections.singletonMap(
                        "secret_key",
                        new File(v4GuardCore.getInstance().getDataFolder(), "vpn.key").exists()
                                ? Files.readAllLines(Paths.get(v4GuardCore.getInstance().getDataFolder() + "/vpn.key", new String[0])).get(0)
                                : null
                );
                backendConnector.setSocket(IO.socket("wss://connector.v4guard.io/minecraft", backendConnector.getOptions()));
                backendConnector.getSocket().connect();
                backendConnector.handleEvents();
            } else if (backendConnector.getSocketStatus().equals(SocketStatus.AUTHENTICATED)) {
                if (backendConnector.isReconnected()) {
                    return;
                }
                Document company = doc.get("company", Document.class);
                v4GuardCore.getInstance().getLogger().log(Level.INFO,"Instance connected using secret key: " + doc.getString("secret"));
                v4GuardCore.getInstance().getLogger().log(Level.INFO,"License assigned to " + company.getString("name") + "/" + company.getString("code"));
                v4GuardCore.getInstance().getLogger().log(Level.INFO,"Plan: " + company.getString("plan"));
                v4GuardCore.getInstance().getLogger().log(Level.INFO,"Manage your settings using the dashboard: https://dashboard.v4guard.io/networks/select/" + company.getString("uuid"));
                backendConnector.setReconnected(true);
            }
        }
        catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
