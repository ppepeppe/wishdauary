package com.wishdiary.application.shop;

import com.wishdiary.domain.notification.GiftRequestNotification;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GiftRequestNotificationResponseMapper {

    public List<GiftRequestNotificationResponse> toResponses(List<GiftRequestNotification> notifications) {
        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    public GiftRequestNotificationResponse toResponse(GiftRequestNotification notification) {
        return new GiftRequestNotificationResponse(
                notification.getId(),
                notification.getProductId(),
                notification.getProductTitle(),
                ShopPhotoResponse.imageUrl(notification.getPhoto(), notification.getProductImageUrl()),
                notification.getProductUrl(),
                notification.getCloverPrice(),
                notification.getPurchasePriceWon(),
                notification.getRequesterChildUserId(),
                notification.getRequesterChildNickname(),
                notification.getReceiverFamilyUserId(),
                notification.getReceiverRelationLabel(),
                notification.getMessage(),
                notification.getStatus().name().toLowerCase(),
                notification.getProcessedByFamilyUserId(),
                notification.getProcessedAt(),
                notification.getCreatedAt()
        );
    }
}
