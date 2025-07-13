package io.v4guard.connector.common.request;

import io.v4guard.connector.common.socket.ActiveConnection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class DefaultHeaderInfoInterceptor implements Interceptor {

    private final ActiveConnection activeConnection;

    public DefaultHeaderInfoInterceptor(ActiveConnection activeConnection) {
        this.activeConnection = activeConnection;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request requestWithHeaders = original
                .newBuilder()
                .header("Content-Type", "application/json")
                .header("User-Agent", "v4Guard API")
                .header("X-V4G-KEY", activeConnection.getSecretKey())
                .build();
        return chain.proceed(requestWithHeaders);
    }
}
