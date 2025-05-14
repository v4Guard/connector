package io.v4guard.connector.common.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BlacklistRequest extends BackendRequest<Boolean> {

    public CompletableFuture<Boolean> addBlacklist(
            Map.Entry<String,String> value,
            String preset,
            String reason,
            boolean ipBan,
            boolean silent,
            boolean propagate,
            String executor
    ) {
        RequestBody body = new FormBody.Builder()
                .add(value.getKey(), value.getValue())
                .add("reason", reason)
                .add("reason_preset", preset)
                .add("ipban", ipBan+"")
                .add("silent", silent+"")
                .add("propagate", propagate+"")
                .add("executor", executor)
                .build();
        
        Request request = new Request.Builder()
                .url("https://dashboard.v4guard.io/api/v1/local/add")
                .post(body)
                .build();

        return responseHandle(request);
    }

    public CompletableFuture<Boolean> removeBlacklist(String id) {
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .build();

        Request request = new Request.Builder()
                .url("https://dashboard.v4guard.io/api/v1/local/delete")
                .delete(body)
                .build();

        return responseHandle(request);
    }
    @Override
    public Boolean onResponse(Call call, Response response) throws IOException {
        ObjectNode node = CoreInstance.get().getObjectMapper().readValue(response.body().string(), ObjectNode.class);

        return node.get("success").asBoolean();
    }

    @Override
    public void onFailure(Call call, IOException e) {
        UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while requesting blacklist ", e);
    }
}
