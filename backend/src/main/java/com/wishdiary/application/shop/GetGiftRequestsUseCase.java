package com.wishdiary.application.shop;

import java.util.List;

public interface GetGiftRequestsUseCase {

    List<GiftRequestNotificationResponse> getReceivedRequests(Long familyUserId);
}
