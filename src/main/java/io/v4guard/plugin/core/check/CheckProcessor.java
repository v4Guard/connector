package io.v4guard.plugin.core.check;

import io.v4guard.plugin.core.check.common.VPNCheck;

public interface CheckProcessor<K> {

    void onPreLogin(String username, K event);
    void onPostLogin(String username, K event);
    boolean onExpire(VPNCheck VPNCheck);
    void kickPlayer(String username, String reason);
}
