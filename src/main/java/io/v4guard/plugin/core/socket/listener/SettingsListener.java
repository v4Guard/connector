package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.socket.RemoteSettings;
import org.bson.Document;

public class SettingsListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        RemoteSettings.overrideBy(Document.parse(args[0].toString()));
    }
}