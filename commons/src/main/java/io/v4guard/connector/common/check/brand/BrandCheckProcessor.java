package io.v4guard.connector.common.check.brand;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.api.constants.SettingsKeys;
import io.v4guard.connector.common.socket.settings.DefaultActiveSettings;
import io.v4guard.connector.common.utils.ProtocolUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BrandCheckProcessor {

    public static final List<String> MODERN_LABYMOD_CHANNELS = List.of("labymod3:main", "labymod:neo");
    public static final String LEGACY_LABYMOD_CHANNEL = "LMC";

    private final Cache<UUID, BrandCallbackTask> PENDING_CHECKS = Caffeine
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .removalListener((key, value, cause) -> ((BrandCallbackTask)value).complete())
            .build();

    public void process(String username, UUID playerUUID, String channel, byte[] bytes) {
        if (!CoreInstance.get().getRemoteConnection().isReady() || !isAllowed(channel)) {
            return;
        }

        DefaultActiveSettings defaultActiveSettings = CoreInstance.get().getActiveSettings();
        boolean invalidatedCache = defaultActiveSettings.getGeneralSetting(SettingsKeys.INVALIDATE_CACHE, false);

        if (invalidatedCache && !defaultActiveSettings.getPrivacySetting(SettingsKeys.COLLECT_MC_BRAND)) {
            return;
        }

        PlayerCheckData checkData = CoreInstance.get().getCheckDataCache().get(username);

        if (checkData == null || checkData.isPlayerBrandChecked()) {
            return;
        }

        BrandCallbackTask task = PENDING_CHECKS.getIfPresent(playerUUID);

        if (task == null) {
            task = new BrandCallbackTask(username, checkData);
            task.start();
            PENDING_CHECKS.put(playerUUID, task);
        }

        if (isLabyMod(channel)) {
            String brand = "labymod:";

            try {
                ByteBuf buf = Unpooled.wrappedBuffer(bytes);
                String key = ProtocolUtils.readString(buf);

                if (key.equals("INFO")) {
                    String json = ProtocolUtils.readString(buf);

                    JsonNode parsedInfo = CoreInstance.get().getObjectMapper().readTree(json);
                    brand += parsedInfo.get("version").asText("unknown");
                } else {
                    return;
                }
            } catch (Exception exception) {
                brand += "unknown";
            }

            task.addBrand(brand);
        } else {
            task.addBrand(new String(bytes));
        }
    }

    public void onPlayerDisconnect(UUID playerUUID) {
        PENDING_CHECKS.invalidate(playerUUID);
    }

    public boolean isAllowed(String channel) {
        return channel.equals("MC|Brand") || channel.equals("minecraft:brand") || isLabyMod(channel);
    }

    public boolean isLabyMod(String channel) {
        return MODERN_LABYMOD_CHANNELS.contains(channel) || LEGACY_LABYMOD_CHANNEL.equals(channel);
    }

    public void handleTick() {
        PENDING_CHECKS.cleanUp();
    }

}