package io.v4guard.connector.api.socket;

import java.util.concurrent.ConcurrentHashMap;

public interface ActiveSettings {

    /**
     * Get the general settings of a company.
     *
     * @return the general settings
     */
    ConcurrentHashMap<String, Boolean> getGeneral();

    /**
     * Get the active addons of a company.
     *
     * @return the active addons
     */
    ConcurrentHashMap<String, Boolean> getActiveAddons();

    /**
     * Get the privacy settings of a company.
     *
     * @return the privacy settings
     */
    ConcurrentHashMap<String, Boolean> getPrivacySettings();
}
