package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

import java.util.List;

public class KickListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        String username = doc.getString("username");
        List<String> reason = doc.get("message", List.class);
        String formattedReason = "", sep = "";
        for (String s : reason) {
            formattedReason += sep + s;
            sep = "\\n";
        }
        for(CheckProcessor cp : v4GuardCore.getInstance().getCheckManager().getProcessors()){
            cp.kickPlayer(username, formattedReason);
        }
    }
}
