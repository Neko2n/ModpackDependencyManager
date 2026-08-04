package com.nekotune.mdm.core;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Event<T> {

    private final List<Consumer<T>> connections = new LinkedList<>();

    public final Hook<T> hook = new Hook<>(this);
    public final Controller<T> controller = new Controller<>(this);

    protected void connect(final Consumer<T> runnable) {
        connections.add(runnable);
    }

    protected void post(final T packet) {
        for (final Consumer<T> connection : connections) {
            connection.accept(packet);
        }
    }

    protected void clear() {
        connections.clear();
    }

    public static record Hook<T>(Event<T> event) {
        public final void connect(final Consumer<T> consumer) {
            event.connect(consumer);
        }
        public final void connect(final Runnable runnable) {
            event.connect($ -> runnable.run());
        }
    }

    public static record Controller<T>(Event<T> event) {
        public final void post(final T packet) {
            event.post(packet);
        }
        public final void clear() {
            event.clear();
        }
    }
}