package com.wishdiary.adapter.out.persistence.wishgoal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishGoalJpaRepository extends JpaRepository<WishGoalJpaEntity, Long> {

    Optional<WishGoalJpaEntity> findByChildUserId(Long childUserId);

    void deleteByChildUserId(Long childUserId);
}
