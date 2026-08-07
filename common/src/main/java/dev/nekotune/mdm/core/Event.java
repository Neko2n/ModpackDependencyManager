package dev.nekotune.mdm.core;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Event<T> {

    private final Set<Consumer<T>> connections = new LinkedHashSet<>();

    public final Hook<T> hook = new Hook<>(this);
    public final Controller<T> controller = new Controller<>(this);

    protected Connection connect(final Consumer<T> runnable) {
        this.connections.add(runnable);
        return new Connection(() -> this.connections.remove(runnable));
    }

    protected void post(final T packet) {
        for (final Consumer<T> connection : this.connections) {
            connection.accept(packet);
        }
    }

    protected void clear() {
        this.connections.clear();
    }

    public static record Hook<T>(Event<T> event) {
        public final Connection connect(final Consumer<T> consumer) {
            return this.event.connect(consumer);
        }

        public final Connection connect(final Runnable runnable) {
            return this.event.connect($ -> runnable.run());
        }
    }

    public static record Controller<T>(Event<T> event) {
        public final void post(final T packet) {
            this.event.post(packet);
        }

        public final void clear() {
            this.event.clear();
        }
    }

    public static class Flag<T> extends Event<T> {
        private boolean state = false;

        public boolean state() {
            return this.state;
        }

        @Override
        protected Connection connect(final Consumer<T> runnable) {
            if (!state) {
                return super.connect(runnable);
            }
            return new Connection(() -> {});
        }

        @Override
        protected void post(final T packet) {
            if (!state) {
                state = true;
                super.post(packet);
                this.clear();
            }
        }
    }

    public static class Connection {

        private final Runnable onDisconnect;

        private Connection(final Runnable onDisconnect) {
            this.onDisconnect = onDisconnect;
        }

        public void disconnect() {
            onDisconnect.run();
        }
    }
}