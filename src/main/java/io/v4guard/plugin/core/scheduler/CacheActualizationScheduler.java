package io.v4guard.plugin.core.scheduler;

import io.v4guard.plugin.core.CoreInstance;

public class CacheActualizationScheduler implements Runnable {

    @Override
    public void run() {
        CoreInstance.get().getChecksCache().cleanup();
        CoreInstance.get().getPlugin().getBrandCheckProcessor().actualize();
    }
}
