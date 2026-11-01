package com.wishdiary.application.shop;

import com.wishdiary.domain.notification.GiftRequestNotification;
import java.util.List;
import java.util.Optional;

public interface LoadGiftRequestNotificationPort {

    Optional<GiftRequestNotification> loadGiftRequestById(Long requestId);

    List<GiftRequestNotification> loadAllByReceiverFamilyUserId(Long familyUserId);

    List<GiftRequestNotification> loadAllByRequesterChildUserId(Long childUserId);
}
