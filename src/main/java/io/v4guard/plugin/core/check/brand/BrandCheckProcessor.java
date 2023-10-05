package io.v4guard.plugin.core.check.brand;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.PlayerCheckData;
import io.v4guard.plugin.core.constants.SettingsKeys;
import io.v4guard.plugin.core.socket.RemoteSettings;
import io.v4guard.plugin.core.utils.ProtocolUtils;
import org.bson.Document;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class BrandCheckProcessor {

    public static final List<String> MODERN_LABYMOD_CHANNELS = List.of("labymod3:main", "labymod:neo");
    public static final String LEGACY_LABYMOD_CHANNEL = "LMC";

    private final Cache<UUID, BrandCallbackTask> PENDING_CHECKS = Caffeine
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .removalListener((key, value, cause) -> {
                BrandCheckProcessor.this.FINALLY_CHECKED.add((UUID) key);
                ((BrandCallbackTask)value).complete();
            })
            .build();

    private final CopyOnWriteArrayList<UUID> FINALLY_CHECKED = new CopyOnWriteArrayList<>();

    public void process(String username, UUID playerUUID, String channel, byte[] bytes) {
        if (!CoreInstance.get().getBackend().isReady()) {
            return;
        }

        if (!isAllowed(channel)) {
            return;
        }

        Document privacySettings = RemoteSettings.getOrDefault(SettingsKeys.PRIVACY, new Document());
        boolean invalidatedCache = RemoteSettings.getOrDefault(SettingsKeys.INVALIDATE_CACHE, false);

        if (invalidatedCache && !privacySettings.getBoolean(SettingsKeys.COLLECT_MC_BRAND, true)) {
            return;
        }

        if (FINALLY_CHECKED.contains(playerUUID)) {
            return;
        }

        PlayerCheckData checkData = CoreInstance.get().getCheckDataCache().get(username);

        if (checkData == null) {
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
                    Document parsedInfo = Document.parse(json);
                    brand += parsedInfo.getOrDefault("version", "unknown");
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
        FINALLY_CHECKED.remove(playerUUID);
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