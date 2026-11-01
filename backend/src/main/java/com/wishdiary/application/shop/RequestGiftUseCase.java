package com.wishdiary.application.shop;

public interface RequestGiftUseCase {

    GiftRequestResponse request(Long childUserId, RequestGiftRequest request);
}
