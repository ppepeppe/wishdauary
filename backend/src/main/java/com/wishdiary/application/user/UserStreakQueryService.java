package com.wishdiary.application.user;

import com.wishdiary.domain.user.UserStreakStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserStreakQueryService implements GetUserStreakUseCase {

    private final UpdateUserStreakPort updateUserStreakPort;
    private final UserStreakResponseMapper userStreakResponseMapper;

    public UserStreakQueryService(
            UpdateUserStreakPort updateUserStreakPort,
            UserStreakResponseMapper userStreakResponseMapper
    ) {
        this.updateUserStreakPort = updateUserStreakPort;
        this.userStreakResponseMapper = userStreakResponseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserStreakResponse getStreak(Long userId) {
        UserStreakStats userStreakStats = updateUserStreakPort.loadUserStreak(userId)
                .orElse(new UserStreakStats(userId, 0, 0, null));
        return userStreakResponseMapper.toResponse(userStreakStats);
    }
}
