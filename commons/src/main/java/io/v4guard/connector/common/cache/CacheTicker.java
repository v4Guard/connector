package io.v4guard.connector.common.cache;

import io.v4guard.connector.common.CoreInstance;

public class CacheTicker implements Runnable {

    private final CoreInstance coreInstance;

    public CacheTicker(CoreInstance coreInstance) {
        this.coreInstance = coreInstance;
    }

    @Override
    public void run() {
        coreInstance.getCheckDataCache().handleTick(coreInstance.getPendingTasks());
        coreInstance.getPlugin().getBrandCheckProcessor().handleTick();
        coreInstance.getPlugin().getPlayerSettingsCheckProcessor().handleTick();
    }

}