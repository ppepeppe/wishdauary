package com.wishdiary.integration;

import java.util.concurrent.atomic.AtomicLong;

final class TestNicknames {
    private static final AtomicLong NEXT = new AtomicLong();
    private TestNicknames() {}
    static String unique(String base) {
        return base.substring(0, Math.min(16, base.length())) + "_" + Long.toString(NEXT.incrementAndGet(), 36);
    }
}
