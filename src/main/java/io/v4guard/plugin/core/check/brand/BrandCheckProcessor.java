package io.v4guard.plugin.core.check.brand;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.PlayerCheckData;
import io.v4guard.plugin.core.constants.SettingsKeys;
import io.v4guard.plugin.core.socket.RemoteSettings;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class BrandCheckProcessor {

    private final Cache<UUID, BrandCallbackTask> PENDING_CHECKS = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .removalListener((entry) -> {
                if (entry.getCause() != RemovalCause.EXPIRED) {
                    return;
                }

                BrandCheckProcessor.this.FINALLY_CHECKED.add((UUID) entry.getKey());
                ((BrandCallbackTask)entry.getValue()).complete();
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

        PlayerCheckData checkData = CoreInstance.get().getChecksCache().get(username);

        if (checkData == null) {
            return;
        }

        BrandCallbackTask task = PENDING_CHECKS.getIfPresent(playerUUID);

        if (task == null) {
            task = new BrandCallbackTask(username, checkData);
            PENDING_CHECKS.put(playerUUID, task);
        }

        if (isLabyMod(channel)) {
            ByteArrayDataInput dataInput = ByteStreams.newDataInput(bytes);
            String key = dataInput.readUTF();

            try {
                Document parsedInfo;

                if (key.equals("INFO")) {
                    parsedInfo = Document.parse(dataInput.readUTF());
                } else {
                    parsedInfo = Document.parse(key);
                }

                task.addBrand("labymod:" + parsedInfo.getOrDefault("version", "unknown"));
            } catch (Exception exception) {
                task.addBrand("labymod:unknown");
            }
        } else {
            task.addBrand(new String(bytes));
        }
    }

    public void onPlayerDisconnect(UUID playerUUID) {
        PENDING_CHECKS.invalidate(playerUUID);
        FINALLY_CHECKED.remove(playerUUID);
        //CoreInstance.get().getPendingTasks().remove(username);
    }

    public boolean isAllowed(String channel) {
        return channel.equals("MC|Brand") || channel.equals("minecraft:brand") || isLabyMod(channel);
    }

    public boolean isLabyMod(String channel) {
        return channel.equals("labymod3:main") || channel.equals("labymod:neo") || channel.equals("LMC");
    }

    public void actualize() {
        PENDING_CHECKS.cleanUp();
    }

}
