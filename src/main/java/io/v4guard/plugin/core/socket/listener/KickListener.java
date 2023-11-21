package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.utils.StringUtils;
import org.bson.Document;

import java.util.List;

public class KickListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        String username = doc.getString("username");
        List<String> rawReason = doc.get("message", List.class);
        String reason = StringUtils.buildMultilineString(rawReason);

        CoreInstance.get().getPlugin().kickPlayer(username, reason);
    }
}
