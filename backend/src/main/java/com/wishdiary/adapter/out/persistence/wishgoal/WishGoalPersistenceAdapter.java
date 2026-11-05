package com.wishdiary.adapter.out.persistence.wishgoal;

import com.wishdiary.application.wishgoal.LoadWishGoalPort;
import com.wishdiary.application.wishgoal.SaveWishGoalPort;
import com.wishdiary.domain.wishgoal.WishGoal;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class WishGoalPersistenceAdapter implements LoadWishGoalPort, SaveWishGoalPort {

    private final WishGoalJpaRepository wishGoalJpaRepository;

    public WishGoalPersistenceAdapter(WishGoalJpaRepository wishGoalJpaRepository) {
        this.wishGoalJpaRepository = wishGoalJpaRepository;
    }

    @Override
    public Optional<WishGoal> loadByChildUserId(Long childUserId) {
        return wishGoalJpaRepository.findByChildUserId(childUserId).map(this::toDomain);
    }

    @Override
    public WishGoal save(WishGoal wishGoal) {
        return toDomain(wishGoalJpaRepository.save(toEntity(wishGoal)));
    }

    @Override
    public void deleteByChildUserId(Long childUserId) {
        wishGoalJpaRepository.deleteByChildUserId(childUserId);
    }

    private WishGoalJpaEntity toEntity(WishGoal wishGoal) {
        return new WishGoalJpaEntity(
                wishGoal.getId(),
                wishGoal.getChildUserId(),
                wishGoal.getProductId(),
                wishGoal.getCreatedAt()
        );
    }

    private WishGoal toDomain(WishGoalJpaEntity entity) {
        return WishGoal.reconstitute(
                entity.getId(),
                entity.getChildUserId(),
                entity.getProductId(),
                entity.getCreatedAt()
        );
    }
}
