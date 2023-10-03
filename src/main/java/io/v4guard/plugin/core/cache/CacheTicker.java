package io.v4guard.plugin.core.cache;

import io.v4guard.plugin.core.CoreInstance;

public class CacheTicker implements Runnable {

    @Override
    public void run() {
        CoreInstance.get().getCheckDataCache().handleTick();
        CoreInstance.get().getPlugin().getBrandCheckProcessor().handleTick();
    }

}