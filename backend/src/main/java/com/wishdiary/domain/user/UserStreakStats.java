package com.wishdiary.domain.user;

import java.time.LocalDate;

public record UserStreakStats(
        Long userId,
        int currentStreak,
        int maxStreak,
        LocalDate lastWrittenDate
) {

    public static UserStreakStats initialize(Long userId, LocalDate writtenDate) {
        return new UserStreakStats(userId, 1, 1, writtenDate);
    }

    public UserStreakStats updateForWrittenDate(LocalDate writtenDate) {
        if (lastWrittenDate == null) {
            return initialize(userId, writtenDate);
        }

        if (lastWrittenDate.isEqual(writtenDate)) {
            return this;
        }

        if (lastWrittenDate.plusDays(1).isEqual(writtenDate)) {
            int nextCurrentStreak = currentStreak + 1;
            return new UserStreakStats(
                    userId,
                    nextCurrentStreak,
                    Math.max(maxStreak, nextCurrentStreak),
                    writtenDate
            );
        }

        return new UserStreakStats(userId, 1, Math.max(maxStreak, 1), writtenDate);
    }
}
