package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.socket.RemoteSettings;
import org.bson.Document;

public class SettingListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        RemoteSettings.updateValue(doc.getString("key"), doc.get("value"));
    }
}
