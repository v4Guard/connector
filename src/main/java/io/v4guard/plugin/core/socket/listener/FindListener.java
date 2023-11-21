package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.compatibility.PlayerFetchResult;
import io.v4guard.plugin.core.socket.Backend;
import org.bson.Document;

public class FindListener implements Emitter.Listener {

    private Backend backend;

    public FindListener(Backend backend) {
        this.backend = backend;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());

        if (doc.containsKey("username")) {
            String taskID = doc.getString("taskID");
            String username = doc.getString("username");
            PlayerFetchResult<?> fetchResult = CoreInstance.get().getPlugin().fetchPlayer(username);

            if (fetchResult.getServerName() != null) {
                backend.getSocket().emit("find"
                        , new Document("taskID", taskID)
                                .append("username", username)
                                .append("location", fetchResult.getServerName())
                                .toJson()
                );
            }
        }
    }

}
