package io.v4guard.connector.common.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class WhitelistRequest extends BackendRequest<Boolean> {

    public CompletableFuture<Boolean> addWhitelist(String player, String executor) {
        RequestBody body = new FormBody.Builder()
                .add("executor", executor)
                .build();
        Request request = new Request.Builder()
                .url("https://dashboard.v4guard.io/api/v1/whitelist/" + player)
                .put(body)
                .build();

        return responseHandle(request);
    }

    public CompletableFuture<Boolean> removeWhitelist(String player) {
        Request request = new Request.Builder()
                .url("https://dashboard.v4guard.io/api/v1/whitelist/" + player)
                .delete()
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
        UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while requesting whitelist ", e);
    }
}
