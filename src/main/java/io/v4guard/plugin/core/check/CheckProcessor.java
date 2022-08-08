package io.v4guard.plugin.core.check;

public interface CheckProcessor<K> {

    void onPreLogin(String username, K event);
    void onLogin(String username, K event);
    void onPostLogin(String username, K event);

}
