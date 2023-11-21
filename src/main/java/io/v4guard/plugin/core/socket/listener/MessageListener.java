package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.compatibility.Messenger;
import io.v4guard.plugin.core.constants.ListenersConstants;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MessageListener implements Emitter.Listener {

    @SuppressWarnings("unchecked")
    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        String permission = (String) doc.getOrDefault("permission", ListenersConstants.NO_PERMISSION_NEEDED);
        List<String> players = (List<String>) doc.getOrDefault("players", new ArrayList<>());
        String message = (String) doc.getOrDefault("message", "Disconnected");
        Messenger messenger = CoreInstance.get().getPlugin().getMessenger();

        if (!permission.equals(ListenersConstants.NO_PERMISSION_NEEDED)) {
            messenger.broadcastWithPermission(message, permission);
        } else for (String player : players) {
            messenger.sendMessageTo(player, message);
        }
    }
}
