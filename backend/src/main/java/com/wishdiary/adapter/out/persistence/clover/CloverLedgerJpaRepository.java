package com.wishdiary.adapter.out.persistence.clover;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CloverLedgerJpaRepository extends JpaRepository<CloverLedgerJpaEntity, Long> {

    @Query("select coalesce(sum(cl.amount), 0) from CloverLedgerJpaEntity cl where cl.userId = :userId")
    Integer sumAmountByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndReasonAndRefId(Long userId, String reason, Long refId);

    List<CloverLedgerJpaEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
