package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

public class FindListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public FindListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        if(doc.containsKey("username")){
            String taskID = doc.getString("taskID");
            String username = doc.getString("username");
            //Obviously the first check processor in the list of processors is the one that is active.
            if(v4GuardCore.getInstance().getCheckManager().getProcessors().get(0).isPlayerOnline(username)){
                String server = v4GuardCore.getInstance().getCheckManager().getProcessors().get(0).getPlayerServer(username);
                backendConnector.getSocket().emit("find",
                        new Document("taskID", taskID)
                                .append("username", username)
                                .append("location", server)
                                .toJson()
                );
            }
        }
    }

}
