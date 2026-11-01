package com.wishdiary.application.shop;

import com.wishdiary.domain.notification.GiftRequestNotification;

public interface SaveGiftRequestNotificationPort {

    GiftRequestNotification save(GiftRequestNotification notification);
}
