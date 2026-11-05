package com.wishdiary.domain.notification;

import java.time.LocalDateTime;
import com.wishdiary.domain.shop.ShopPhoto;

public class GiftRequestNotification {

    private final ShopPhoto photo;
    private final Long id;
    private final Long productId;
    private final String productTitle;
    private final String productImageUrl;
    private final String productUrl;
    private final int cloverPrice;
    private final Integer purchasePriceWon;
    private final Long requesterChildUserId;
    private final String requesterChildNickname;
    private final Long receiverFamilyUserId;
    private final String receiverRelationLabel;
    private final String message;
    private final GiftRequestStatus status;
    private final Long processedByFamilyUserId;
    private final LocalDateTime processedAt;
    private final LocalDateTime createdAt;

    private GiftRequestNotification(
            Long id,
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
            GiftRequestStatus status,
            Long processedByFamilyUserId,
            LocalDateTime processedAt,
            LocalDateTime createdAt
    ) {
        this(id, productId, productTitle, productImageUrl, productUrl, cloverPrice, purchasePriceWon,
                requesterChildUserId, requesterChildNickname, receiverFamilyUserId, receiverRelationLabel,
                message, status, processedByFamilyUserId, processedAt, createdAt, null);
    }

    private GiftRequestNotification(Long id, Long productId, String productTitle, String productImageUrl,
            String productUrl, int cloverPrice, Integer purchasePriceWon, Long requesterChildUserId,
            String requesterChildNickname, Long receiverFamilyUserId, String receiverRelationLabel,
            String message, GiftRequestStatus status, Long processedByFamilyUserId,
            LocalDateTime processedAt, LocalDateTime createdAt, ShopPhoto photo) {
        this.photo = photo;
        this.id = id;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productImageUrl = productImageUrl;
        this.productUrl = productUrl;
        this.cloverPrice = cloverPrice;
        this.purchasePriceWon = purchasePriceWon;
        this.requesterChildUserId = requesterChildUserId;
        this.requesterChildNickname = requesterChildNickname;
        this.receiverFamilyUserId = receiverFamilyUserId;
        this.receiverRelationLabel = receiverRelationLabel;
        this.message = message;
        this.status = status;
        this.processedByFamilyUserId = processedByFamilyUserId;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
    }

    public static GiftRequestNotification create(
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
            String message
    ) {
        return new GiftRequestNotification(
                null,
                productId,
                productTitle,
                productImageUrl,
                productUrl,
                cloverPrice,
                purchasePriceWon,
                requesterChildUserId,
                requesterChildNickname,
                receiverFamilyUserId,
                receiverRelationLabel,
                message,
                GiftRequestStatus.PENDING,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public static GiftRequestNotification reconstitute(
            Long id,
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
            GiftRequestStatus status,
            Long processedByFamilyUserId,
            LocalDateTime processedAt,
            LocalDateTime createdAt
    ) {
        return new GiftRequestNotification(
                id,
                productId,
                productTitle,
                productImageUrl,
                productUrl,
                cloverPrice,
                purchasePriceWon,
                requesterChildUserId,
                requesterChildNickname,
                receiverFamilyUserId,
                receiverRelationLabel,
                message,
                status,
                processedByFamilyUserId,
                processedAt,
                createdAt
        );
    }

    public GiftRequestNotification approve(Long familyUserId) {
        return process(familyUserId, GiftRequestStatus.APPROVED);
    }

    public GiftRequestNotification reject(Long familyUserId) {
        return process(familyUserId, GiftRequestStatus.REJECTED);
    }

    private GiftRequestNotification process(Long familyUserId, GiftRequestStatus nextStatus) {
        if (status != GiftRequestStatus.PENDING) {
            throw new com.wishdiary.domain.shop.GiftRequestAlreadyProcessedException(id);
        }
        return new GiftRequestNotification(
                id,
                productId,
                productTitle,
                productImageUrl,
                productUrl,
                cloverPrice,
                purchasePriceWon,
                requesterChildUserId,
                requesterChildNickname,
                receiverFamilyUserId,
                receiverRelationLabel,
                message,
                nextStatus,
                familyUserId,
                LocalDateTime.now(),
                createdAt,
                photo
        );
    }

    public GiftRequestNotification withPhoto(ShopPhoto photo) {
        return new GiftRequestNotification(id, productId, productTitle, productImageUrl, productUrl,
                cloverPrice, purchasePriceWon, requesterChildUserId, requesterChildNickname,
                receiverFamilyUserId, receiverRelationLabel, message, status, processedByFamilyUserId,
                processedAt, createdAt, photo);
    }

    public ShopPhoto getPhoto() { return photo; }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public int getCloverPrice() {
        return cloverPrice;
    }

    public Integer getPurchasePriceWon() {
        return purchasePriceWon;
    }

    public Long getRequesterChildUserId() {
        return requesterChildUserId;
    }

    public String getRequesterChildNickname() {
        return requesterChildNickname;
    }

    public Long getReceiverFamilyUserId() {
        return receiverFamilyUserId;
    }

    public String getReceiverRelationLabel() {
        return receiverRelationLabel;
    }

    public String getMessage() {
        return message;
    }

    public GiftRequestStatus getStatus() {
        return status;
    }

    public Long getProcessedByFamilyUserId() {
        return processedByFamilyUserId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
