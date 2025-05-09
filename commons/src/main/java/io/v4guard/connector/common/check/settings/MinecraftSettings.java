package io.v4guard.connector.common.check.settings;

import io.v4guard.connector.api.constants.SettingsCheckConstants;

import java.util.HashMap;
import java.util.Map;

public class MinecraftSettings {
    private final String locale;
    private final String viewDistance;
    private final String colors;
    private final String mainHand;
    private final String chatMode;
    private final String clientListing;
    private final String hat;
    private final String cape;
    private final String jacket;
    private final String leftSleeve;
    private final String rightSleeve;
    private final String leftPants;
    private final String rightPants;

    public MinecraftSettings(
              String locale,
              String viewDistance,
              String colors,
              String mainHand,
              String chatMode,
              String clientListing,
              String hat,
              String cape,
              String jacket,
              String leftSleeve,
              String rightSleeve,
              String leftPants,
              String rightPants
    ) {
        this.locale = locale;
        this.viewDistance = viewDistance;
        this.colors = colors;
        this.mainHand = mainHand;
        this.chatMode = chatMode;
        this.clientListing = clientListing;
        this.hat = hat;
        this.cape = cape;
        this.jacket = jacket;
        this.leftSleeve = leftSleeve;
        this.rightSleeve = rightSleeve;
        this.leftPants = leftPants;
        this.rightPants = rightPants;
    }

    public static MinecraftSettings.Builder builder() {
        return new Builder();
    }

    public Map<String, String> getMainSettingsMap() {
        Map<String, String> map = new HashMap<>();

        map.put(SettingsCheckConstants.LOCALE, locale);
        map.put(SettingsCheckConstants.VIEW_DISTANCE, viewDistance);
        map.put(SettingsCheckConstants.COLORS, colors);
        map.put(SettingsCheckConstants.MAIN_HAND, mainHand);
        map.put(SettingsCheckConstants.CHAT_MODE, chatMode);

        if (clientListing != null) {
            map.put(SettingsCheckConstants.CLIENT_LISTING_ALLOWED, clientListing);
        }

        return map;
    }

    public Map<String, String> getSkinSettingsMap() {
        Map<String, String> map = new HashMap<>();

        map.put(SettingsCheckConstants.SkinParts.HAT, hat);
        map.put(SettingsCheckConstants.SkinParts.CAPE, cape);
        map.put(SettingsCheckConstants.SkinParts.JACKET, jacket);
        map.put(SettingsCheckConstants.SkinParts.LEFT_SLEEVE, leftSleeve);
        map.put(SettingsCheckConstants.SkinParts.RIGHT_SLEEVE, rightSleeve);
        map.put(SettingsCheckConstants.SkinParts.LEFT_PANTS, leftPants);
        map.put(SettingsCheckConstants.SkinParts.RIGHT_PANTS, rightPants);

        return map;
    }

    public static class Builder {
        private String locale;
        private String viewDistance;
        private String colors;
        private String mainHand;
        private String chatMode;
        private String clientListing;
        private String hat;
        private String cape;
        private String jacket;
        private String leftSleeve;
        private String rightSleeve;
        private String hasLeftPants;
        private String hasRightPants;

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder viewDistance(String viewDistance) {
            this.viewDistance = viewDistance;
            return this;
        }

        public Builder hasColors(String colors) {
            this.colors = colors;
            return this;
        }

        public Builder mainHand(String mainHand) {
            this.mainHand = mainHand;
            return this;
        }

        public Builder chatMode(String chatMode) {
            this.chatMode = chatMode;
            return this;
        }

        public Builder clientListing(String clientListing) {
            this.clientListing = clientListing;
            return this;
        }

        public Builder hasHat(String hat) {
            this.hat = hat;
            return this;
        }

        public Builder hasCape(String cape) {
            this.cape = cape;
            return this;
        }

        public Builder hasJacket(String jacket) {
            this.jacket = jacket;
            return this;
        }

        public Builder hasLeftSleeve(String leftSleeve) {
            this.leftSleeve = leftSleeve;
            return this;
        }

        public Builder hasRightSleeve(String rightSleeve) {
            this.rightSleeve = rightSleeve;
            return this;
        }

        public Builder hasLeftPants(String leftPants) {
            this.hasLeftPants = leftPants;
            return this;
        }

        public Builder hasRightPants(String rightPants) {
            this.hasRightPants = rightPants;
            return this;
        }

        public MinecraftSettings build() {
            return new MinecraftSettings(
                    locale,
                    viewDistance,
                    colors,
                    mainHand,
                    chatMode,
                    clientListing,
                    hat,
                    cape,
                    jacket,
                    leftSleeve,
                    rightSleeve,
                    hasLeftPants,
                    hasRightPants
            );
        }
    }
}
