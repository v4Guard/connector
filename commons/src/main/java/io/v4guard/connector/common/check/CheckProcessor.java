package io.v4guard.connector.common.check;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.nickname.NicknameCallbackTask;
import io.v4guard.connector.common.check.vpn.VPNCallbackTask;
import io.v4guard.connector.api.constants.SettingsKeys;
import io.v4guard.connector.common.socket.settings.DefaultActiveSettings;
import io.v4guard.connector.common.utils.HostnameUtils;

public abstract class CheckProcessor<E> {

    public void onEvent(String username, E event) {

    }

    public PlayerCheckData prepareCheckData(String username, String address, int version, String virtualHostTemp, boolean bedrock) {
        DefaultActiveSettings defaultActiveSettings = CoreInstance.get().getActiveSettings();
        boolean anonVirtualHost = defaultActiveSettings.getPrivacySetting(SettingsKeys.ANON_VIRTUAL_HOST);
        boolean waitMode = defaultActiveSettings.getGeneralSetting(SettingsKeys.WAIT_RESPONSE, false);

        String virtualHost = HostnameUtils.detectVirtualHost(virtualHostTemp, anonVirtualHost);
        PlayerCheckData checkData = new PlayerCheckData(username, address, version, virtualHost, waitMode, bedrock);

        checkData.addTask(new NicknameCallbackTask(checkData));
        checkData.addTask(new VPNCallbackTask(checkData));

        return checkData;
    }
}
