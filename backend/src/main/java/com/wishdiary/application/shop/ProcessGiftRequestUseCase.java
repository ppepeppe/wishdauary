package com.wishdiary.application.shop;

public interface ProcessGiftRequestUseCase {

    GiftRequestNotificationResponse process(Long familyUserId, Long requestId, ProcessGiftRequestRequest request);
}
