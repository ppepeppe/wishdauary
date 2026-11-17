package com.wishdiary.application.user;

import com.wishdiary.domain.user.UserStreakStats;
import java.util.Optional;

public interface UpdateUserStreakPort {

    void lockUser(Long userId);

    Optional<UserStreakStats> loadUserStreak(Long userId);

    UserStreakStats saveUserStreak(UserStreakStats userStreakStats);
}
