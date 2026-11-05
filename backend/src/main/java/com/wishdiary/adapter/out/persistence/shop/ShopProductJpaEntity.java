package com.wishdiary.adapter.out.persistence.shop;

import com.wishdiary.domain.shop.ShopProductSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_products")
public class ShopProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long childUserId;

    @Column
    private Long receiverFamilyUserId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(columnDefinition = "bytea")
    private byte[] imageData;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 1000)
    private String productUrl;

    @Column(nullable = false)
    private int cloverPrice;

    @Column
    private Integer purchasePriceWon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopProductSource source;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ShopProductJpaEntity() {
    }

    public ShopProductJpaEntity(
            Long id,
            Long childUserId,
            String title,
            String description,
            String imageUrl,
            String productUrl,
            int cloverPrice,
            Integer purchasePriceWon,
            ShopProductSource source,
            LocalDateTime createdAt,
            Long receiverFamilyUserId
    ) {
        this.id = id;
        this.childUserId = childUserId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.cloverPrice = cloverPrice;
        this.purchasePriceWon = purchasePriceWon;
        this.source = source;
        this.createdAt = createdAt;
        this.receiverFamilyUserId = receiverFamilyUserId;
    }

    public ShopProductJpaEntity withImageData(byte[] data) {
        this.imageData = data;
        return this;
    }

    public byte[] getImageData() { return imageData; }

    public Long getId() {
        return id;
    }

    public Long getReceiverFamilyUserId() { return receiverFamilyUserId; }

    public Long getChildUserId() {
        return childUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
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

    public ShopProductSource getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
