package com.wishdiary.adapter.out.persistence.wishgoal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishGoalJpaRepository extends JpaRepository<WishGoalJpaEntity, Long> {

    Optional<WishGoalJpaEntity> findByChildUserId(Long childUserId);

    void deleteByChildUserId(Long childUserId);

    @Modifying
    @Query("delete from WishGoalJpaEntity goal where goal.childUserId = :childId and goal.productId = :productId")
    void deleteMatchingGoal(@Param("childId") Long childId, @Param("productId") Long productId);
}
