package com.wishdiary.adapter.out.persistence.shop;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftRequestNotificationJpaRepository extends JpaRepository<GiftRequestNotificationJpaEntity, Long> {

    List<GiftRequestNotificationJpaEntity> findAllByReceiverFamilyUserIdOrderByCreatedAtDesc(Long familyUserId);

    List<GiftRequestNotificationJpaEntity> findAllByRequesterChildUserIdOrderByCreatedAtDesc(Long childUserId);
}
