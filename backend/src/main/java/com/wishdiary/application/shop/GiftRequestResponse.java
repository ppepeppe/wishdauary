package com.wishdiary.application.shop;

import java.time.LocalDateTime;

public record GiftRequestResponse(
        Long notificationId,
        Long productId,
        String productTitle,
        String productUrl,
        int cloverPrice,
        Integer purchasePriceWon,
        Long childUserId,
        String childNickname,
        Long receiverFamilyUserId,
        String receiverRelationLabel,
        String message,
        int remainingCloverBalance,
        String status,
        LocalDateTime createdAt
) {
}
