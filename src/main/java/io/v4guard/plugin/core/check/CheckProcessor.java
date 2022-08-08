package io.v4guard.plugin.core.check;

import io.v4guard.plugin.core.utils.CheckStatus;

public interface CheckProcessor<K> {

    void onPreLogin(String username, K event);
    void onLogin(String username, K event);
    void onPostLogin(String username, K event);

    boolean actionOnExpire(CheckStatus checkStatus);
}
