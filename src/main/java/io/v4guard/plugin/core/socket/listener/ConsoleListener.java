package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;
import java.util.logging.Level;

public class ConsoleListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        String message = (String) doc.getOrDefault("message", null);
        Level lvl = Level.parse((String) doc.getOrDefault("level", "INFO"));
        if(message == null) return;
        v4GuardCore.getInstance().getLogger().log(lvl, message);
    }

}
