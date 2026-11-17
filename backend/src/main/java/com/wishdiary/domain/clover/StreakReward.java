package com.wishdiary.domain.clover;

import java.util.Arrays;
import java.util.Optional;

public enum StreakReward {
    THREE_DAYS(3, 1, CloverReason.STREAK_3),
    FIVE_DAYS(5, 2, CloverReason.STREAK_5),
    SEVEN_DAYS(7, 3, CloverReason.STREAK_7);

    private final int days;
    private final int amount;
    private final CloverReason reason;

    StreakReward(int days, int amount, CloverReason reason) {
        this.days = days;
        this.amount = amount;
        this.reason = reason;
    }

    public int amount() { return amount; }

    public CloverReason reason() { return reason; }

    public static Optional<StreakReward> at(int days) {
        return Arrays.stream(values()).filter(reward -> reward.days == days).findFirst();
    }
}
