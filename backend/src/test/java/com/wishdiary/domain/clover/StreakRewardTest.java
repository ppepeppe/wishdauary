package com.wishdiary.domain.clover;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StreakRewardTest {
    @ParameterizedTest
    @CsvSource({"3,1,STREAK_3", "5,2,STREAK_5", "7,3,STREAK_7"})
    void rewardsExactlyTheRequestedMilestones(int days, int amount, String reason) {
        StreakReward reward = StreakReward.at(days).orElseThrow();
        assertEquals(amount, reward.amount());
        assertEquals(reason, reward.reason().name());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 4, 6, 8, 10, 14})
    void doesNotInventRecurringBonuses(int days) {
        assertTrue(StreakReward.at(days).isEmpty());
    }
}
