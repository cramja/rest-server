package com.cramja.rest.core;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class DataHelpers {

    private static AtomicLong idSource = new AtomicLong(0);

    private DataHelpers() {}

    public static UUID nextId() {
        long nextId = idSource.incrementAndGet();
        return new UUID(0, nextId);
    }

}
