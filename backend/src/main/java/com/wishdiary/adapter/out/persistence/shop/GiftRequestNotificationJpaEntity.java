package com.wishdiary.adapter.out.persistence.shop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.wishdiary.domain.notification.GiftRequestStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_request_notifications")
public class GiftRequestNotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 120)
    private String productTitle;

    @Column(columnDefinition = "bytea")
    private byte[] productImageData;

    @Column(length = 500)
    private String productImageUrl;

    @Column(length = 1000)
    private String productUrl;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int cloverPrice;

    @Column
    private Integer purchasePriceWon;

    @Column(nullable = false)
    private Long requesterChildUserId;

    @Column(nullable = false, length = 100)
    private String requesterChildNickname;

    @Column(nullable = false)
    private Long receiverFamilyUserId;

    @Column(length = 100)
    private String receiverRelationLabel;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PENDING'")
    private GiftRequestStatus status;

    private Long processedByFamilyUserId;

    private LocalDateTime processedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected GiftRequestNotificationJpaEntity() {
    }

    public GiftRequestNotificationJpaEntity(
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

    public GiftRequestNotificationJpaEntity withImageData(byte[] data) {
        this.productImageData = data;
        return this;
    }

    public byte[] getImageData() { return productImageData; }

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
