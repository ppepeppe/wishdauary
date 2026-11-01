package com.wishdiary.application.shop;

import java.time.LocalDateTime;

public record GiftRequestNotificationResponse(
        Long notificationId,
        Long productId,
        String productTitle,
        String productImageUrl,
        String productUrl,
        int cloverPrice,
        Integer purchasePriceWon,
        Long requesterChildUserId,
        String requesterChildNickname,
        Long receiverFamilyUserId,
        String receiverRelationLabel,
        String message,
        String status,
        Long processedByFamilyUserId,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
}
