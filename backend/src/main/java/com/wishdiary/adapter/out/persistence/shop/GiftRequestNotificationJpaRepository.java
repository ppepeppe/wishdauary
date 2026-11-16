package com.wishdiary.adapter.out.persistence.shop;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface GiftRequestNotificationJpaRepository extends JpaRepository<GiftRequestNotificationJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from GiftRequestNotificationJpaEntity request where request.id = :requestId")
    Optional<GiftRequestNotificationJpaEntity> findForUpdate(@Param("requestId") Long requestId);

    List<GiftRequestNotificationJpaEntity> findAllByReceiverFamilyUserIdOrderByCreatedAtDesc(Long familyUserId);

    List<GiftRequestNotificationJpaEntity> findAllByRequesterChildUserIdOrderByCreatedAtDesc(Long childUserId);
}
