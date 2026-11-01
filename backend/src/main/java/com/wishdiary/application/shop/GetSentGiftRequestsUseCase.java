package com.wishdiary.application.shop;

import java.util.List;

public interface GetSentGiftRequestsUseCase {

    List<GiftRequestNotificationResponse> getSentRequests(Long childUserId);
}
