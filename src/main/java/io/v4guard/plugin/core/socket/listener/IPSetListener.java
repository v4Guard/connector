package io.v4guard.plugin.core.socket.listener;

import io.v4guard.plugin.core.socket.BackendConnector;
import io.socket.emitter.Emitter;
import org.bson.Document;

import java.io.IOException;

@Deprecated
public class IPSetListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public IPSetListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        try {
            String action = doc.getString("action");
            if (action.equals("add")) {
                backendConnector.getRuntime().exec(String.format("ipset add v4guard %s timeout %s", doc.getString("ip"), doc.getString("timeout")));
            } else if (action.equals("flush")) {
                backendConnector.getRuntime().exec("ipset flush v4guard");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
