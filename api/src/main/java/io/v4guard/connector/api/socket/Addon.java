package io.v4guard.connector.api.socket;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface Addon {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    ConcurrentHashMap<String,String> getSettings();

    ConcurrentHashMap<String, List<String>> getMessages();
}
