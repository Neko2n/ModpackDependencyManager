package com.nekotune.mdm.definition;

import java.util.LinkedList;
import java.util.List;

public final class Event {

    private final List<Runnable> hooks = new LinkedList<>();

    public final void connect(final Runnable runnable) {
        hooks.add(runnable);
    }

    public final void fire() {
        for (final Runnable hook : hooks) {
            hook.run();
        }
    }
}