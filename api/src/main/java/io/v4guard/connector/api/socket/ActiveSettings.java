package io.v4guard.connector.api.socket;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface ActiveSettings {

    ConcurrentHashMap<String, Boolean> getGeneral();

    ConcurrentHashMap<String, List<String>> getMessages();

    ConcurrentHashMap<String, Addon> getActiveAddons();

    ConcurrentHashMap<String, Boolean> getPrivacySettings();


}
