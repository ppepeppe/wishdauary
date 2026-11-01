package com.wishdiary.application.user;

public record UserStreakResponse(
        int currentStreak,
        int maxStreak
) {
}
