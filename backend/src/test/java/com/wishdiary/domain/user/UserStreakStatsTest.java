package com.wishdiary.domain.user;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UserStreakStatsTest {
    private final LocalDate latest = LocalDate.of(2026, 11, 17);
    private final UserStreakStats stats = new UserStreakStats(1L, 5, 7, latest);

    @Test
    void anotherDiaryOnTheSameDateDoesNotAdvanceTheStreak() {
        assertEquals(stats, stats.updateForWrittenDate(latest));
    }

    @Test
    void addingAnOlderDiaryDoesNotRewindTheActiveStreak() {
        assertEquals(stats, stats.updateForWrittenDate(latest.minusDays(1)));
        assertEquals(stats, stats.updateForWrittenDate(latest.minusDays(20)));
    }

    @Test
    void aGapStartsANewStreakAndKeepsThePersonalRecord() {
        assertEquals(new UserStreakStats(1L, 1, 7, latest.plusDays(2)), stats.updateForWrittenDate(latest.plusDays(2)));
    }
}
