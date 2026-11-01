package com.wishdiary.application.user;

import com.wishdiary.domain.user.UserStreakStats;
import org.springframework.stereotype.Component;

@Component
public class UserStreakResponseMapper {

    public UserStreakResponse toResponse(UserStreakStats userStreakStats) {
        return new UserStreakResponse(
                userStreakStats.currentStreak(),
                userStreakStats.maxStreak()
        );
    }
}
