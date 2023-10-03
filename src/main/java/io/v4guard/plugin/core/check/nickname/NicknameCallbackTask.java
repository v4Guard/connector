package io.v4guard.plugin.core.check.nickname;

import io.v4guard.plugin.core.check.CallbackTask;
import io.v4guard.plugin.core.check.CheckStatus;
import io.v4guard.plugin.core.check.PlayerCheckData;
import io.v4guard.plugin.core.constants.SettingsKeys;
import io.v4guard.plugin.core.socket.RemoteSettings;
import org.bson.Document;

import java.util.regex.Pattern;

public class NicknameCallbackTask extends CallbackTask {

    public NicknameCallbackTask(PlayerCheckData checkData) {
        super(checkData);
    }

    public void start() {
        super.start();

        Document nameValidator = RemoteSettings.get(SettingsKeys.NAME_VALIDATOR);

        if (nameValidator.getBoolean("isEnabled")) {
            String regex = nameValidator.getString("regex");

            if (Pattern.compile("^" + regex + "$").matcher(checkData.getUsername()).matches()) {
                checkData.setCheckStatus(CheckStatus.USER_DENIED);
                checkData.setKickReason(RemoteSettings.getMessage("invalidUsername"));
            }
        }

        complete();
    }
}
