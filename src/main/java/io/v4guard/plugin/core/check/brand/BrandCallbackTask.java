package io.v4guard.plugin.core.check.brand;

import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.CallbackTask;
import io.v4guard.plugin.core.check.PlayerCheckData;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

public class BrandCallbackTask extends CallbackTask {

    private final Set<String> BRANDS;

    public BrandCallbackTask(String taskID, PlayerCheckData checkData) {
        super(taskID, checkData);
        this.BRANDS = new HashSet<>();
    }

    @Override
    public void complete() {
        CoreInstance.get().getBackend().getSocket().emit(
                "mc:brand"
                , new Document()
                        .append("username", checkData.getUsername())
                        .append("brand", BRANDS)
                        .toJson()
        );
    }

    public void addBrand(String brand) {
        this.BRANDS.add(brand);
    }
}
