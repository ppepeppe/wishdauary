package com.wishdiary.adapter.out.persistence.user;

import com.wishdiary.application.user.UpdateUserStreakPort;
import com.wishdiary.domain.user.UserStreakStats;
import com.wishdiary.domain.user.UserNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserStatsPersistenceAdapter implements UpdateUserStreakPort {

    private final UserStatsJpaRepository userStatsJpaRepository;
    private final EntityManager entityManager;

    public UserStatsPersistenceAdapter(UserStatsJpaRepository userStatsJpaRepository, EntityManager entityManager) {
        this.userStatsJpaRepository = userStatsJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void lockUser(Long userId) {
        if (entityManager.find(UserJpaEntity.class, userId, LockModeType.PESSIMISTIC_WRITE) == null) {
            throw new UserNotFoundException(userId);
        }
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
