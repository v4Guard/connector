package io.v4guard.connector.common.check.nickname;


import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.CallbackTask;
import io.v4guard.connector.common.check.CheckStatus;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.common.constants.SettingsKeys;
import io.v4guard.connector.common.socket.ActiveSettings;
import io.v4guard.connector.common.utils.StringUtils;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class NicknameCallbackTask extends CallbackTask {

    public NicknameCallbackTask(PlayerCheckData checkData) {
        super(checkData);
    }

    public void start() {
        super.start();

        ActiveSettings.NameValidator nameValidator = CoreInstance.get().getActiveSettings().getNameValidator();

        if (!nameValidator.isValid(checkData.getUsername())) {
           checkData.setCheckStatus(CheckStatus.USER_DENIED);
           checkData.setKickReason(StringUtils.buildMultilineString(CoreInstance.get().getActiveSettings().getMessage("invalidUsername")));
        }

        complete();
    }
}
