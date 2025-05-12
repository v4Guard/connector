package io.v4guard.connector.common.request;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class WhitelistRequest {

    private final OkHttpClient client;
    private final RequestBody EMPTY_BODY;

    public WhitelistRequest() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new DefaultHeaderInfoInterceptor(CoreInstance.get().getRemoteConnection()))
                .build();
        this.EMPTY_BODY = RequestBody.create(null, new byte[0]);
    }

    public CompletableFuture<Boolean> addWhitelist(String player) {
        Request request = new Request.Builder()
                .url("https://dashboard.v4guard.io/api/v1/whitelist/" + player)
                .put(EMPTY_BODY)
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

    public CompletableFuture<Boolean> responseHandle(Request request) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while requesting to add whitelist ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ObjectNode node = CoreInstance.get().getObjectMapper().readValue(response.body().string(), ObjectNode.class);
                future.complete(node.get("success").asBoolean());

            }
        });

        return future;
    }
}
