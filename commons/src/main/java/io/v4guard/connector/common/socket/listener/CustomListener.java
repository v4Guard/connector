package io.v4guard.connector.common.socket.listener;

import io.socket.emitter.Emitter;

import java.util.function.Consumer;

public class CustomListener implements Emitter.Listener {

    private final Consumer<Object[]> consumer;

    public CustomListener(final Consumer<Object[]> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void call(Object... objects) {
        consumer.accept(objects);
    }

}
