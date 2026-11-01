package com.wishdiary.application.shop;

import com.wishdiary.domain.notification.GiftRequestNotification;
import org.springframework.stereotype.Component;

@Component
public class GiftRequestResponseMapper {

    public GiftRequestResponse toResponse(GiftRequestNotification notification, int cloverPrice, int remainingBalance) {
        return new GiftRequestResponse(
                notification.getId(),
                notification.getProductId(),
                notification.getProductTitle(),
                notification.getProductUrl(),
                cloverPrice,
                notification.getPurchasePriceWon(),
                notification.getRequesterChildUserId(),
                notification.getRequesterChildNickname(),
                notification.getReceiverFamilyUserId(),
                notification.getReceiverRelationLabel(),
                notification.getMessage(),
                remainingBalance,
                notification.getStatus().name().toLowerCase(),
                notification.getCreatedAt()
        );
    }
}
