package com.wishdiary.adapter.out.persistence.user;

import com.wishdiary.application.user.UpdateUserStreakPort;
import com.wishdiary.domain.user.UserStreakStats;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserStatsPersistenceAdapter implements UpdateUserStreakPort {

    private final UserStatsJpaRepository userStatsJpaRepository;

    public UserStatsPersistenceAdapter(UserStatsJpaRepository userStatsJpaRepository) {
        this.userStatsJpaRepository = userStatsJpaRepository;
    }

    @Override
    public Optional<UserStreakStats> loadUserStreak(Long userId) {
        return userStatsJpaRepository.findById(userId)
                .map(this::toDomain);
    }

    @Override
    public UserStreakStats saveUserStreak(UserStreakStats userStreakStats) {
        UserStatsJpaEntity savedEntity = userStatsJpaRepository.save(new UserStatsJpaEntity(
                userStreakStats.userId(),
                userStreakStats.currentStreak(),
                userStreakStats.maxStreak(),
                userStreakStats.lastWrittenDate()
        ));
        return toDomain(savedEntity);
    }

    private UserStreakStats toDomain(UserStatsJpaEntity userStatsJpaEntity) {
        return new UserStreakStats(
                userStatsJpaEntity.getUserId(),
                userStatsJpaEntity.getCurrentStreak(),
                userStatsJpaEntity.getMaxStreak(),
                userStatsJpaEntity.getLastWrittenDate()
        );
    }
}
