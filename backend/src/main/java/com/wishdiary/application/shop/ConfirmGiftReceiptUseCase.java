package com.wishdiary.application.shop;

public interface ConfirmGiftReceiptUseCase {
    GiftRequestNotificationResponse confirmReceipt(Long childUserId, Long requestId);
}
