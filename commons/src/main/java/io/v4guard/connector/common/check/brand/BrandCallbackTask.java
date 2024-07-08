package io.v4guard.connector.common.check.brand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.CallbackTask;
import io.v4guard.connector.common.check.PlayerCheckData;

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
        super.checkData.setPlayerBrandChecked(true);

        ObjectMapper mapper = CoreInstance.get().getObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("username", super.checkData.getUsername())
                .set("brand", mapper.valueToTree(this.BRANDS));

        CoreInstance.get().getRemoteConnection().send(
                "mc:brand"
                , node
        );
    }

    public void addBrand(String brand) {
        this.BRANDS.add(brand);
    }
}
