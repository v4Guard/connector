package io.v4guard.connector.common.request;

import io.v4guard.connector.common.CoreInstance;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public abstract class BackendRequest<T> {

    private final OkHttpClient client;
    private final RequestBody emptyBody;

    public BackendRequest() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new DefaultHeaderInfoInterceptor(CoreInstance.get().getRemoteConnection()))
                .build();
        this.emptyBody = RequestBody.create(null, new byte[0]);
    }

    public CompletableFuture<T> responseHandle(Request request) {
        CompletableFuture<T> future = new CompletableFuture<>();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                BackendRequest.this.onFailure(call, e);
                future.complete(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                future.complete(BackendRequest.this.onResponse(call, response));

            }
        });

        return future;
    }


    public abstract T onResponse(Call call, Response response) throws IOException;

    public abstract void onFailure(Call call, IOException e);

    public RequestBody getEmptyBody() {
        return emptyBody;
    }
}
