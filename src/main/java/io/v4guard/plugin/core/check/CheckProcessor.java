package io.v4guard.plugin.core.check;

import io.v4guard.plugin.core.check.nickname.NicknameCallbackTask;
import io.v4guard.plugin.core.check.vpn.VPNCallbackTask;
import io.v4guard.plugin.core.constants.SettingsKeys;
import io.v4guard.plugin.core.socket.RemoteSettings;
import io.v4guard.plugin.core.utils.HostnameUtils;
import org.bson.Document;

public abstract class CheckProcessor<E> {

    public void onEvent(String username, E event) {

    }

    public PlayerCheckData prepareCheckData(String username, String address, int version, String virtualHostTemp, boolean bedrock) {
        Document privacySettings = RemoteSettings.getOrDefault(SettingsKeys.PRIVACY, new Document());
        boolean anonVirtualHost = privacySettings.getBoolean(SettingsKeys.ANON_VIRTUAL_HOST, true);
        boolean waitMode = RemoteSettings.getOrDefault(SettingsKeys.WAIT_RESPONSE, false);
        String virtualHost = HostnameUtils.detectVirtualHost(virtualHostTemp, anonVirtualHost);
        PlayerCheckData checkData = new PlayerCheckData(username, address, version, virtualHost, waitMode, bedrock);

        checkData.addTask(new NicknameCallbackTask(checkData));
        checkData.addTask(new VPNCallbackTask(checkData));

        return checkData;
    }
}
