package com.wishdiary.domain.shop;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

public class ShopProduct {

    private final Long id;
    private final Long childUserId;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String productUrl;
    private final int cloverPrice;
    private final Integer purchasePriceWon;
    private final ShopProductSource source;
    private final LocalDateTime createdAt;

    private ShopProduct(
            Long id,
            Long childUserId,
            String title,
            String description,
            String imageUrl,
            String productUrl,
            int cloverPrice,
            Integer purchasePriceWon,
            ShopProductSource source,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.childUserId = childUserId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.productUrl = normalizeProductUrl(productUrl);
        this.cloverPrice = cloverPrice;
        this.purchasePriceWon = purchasePriceWon;
        this.source = source;
        this.createdAt = createdAt;
    }

    private String normalizeProductUrl(String productUrl) {
        if (productUrl == null || productUrl.isBlank()) {
            return null;
        }

        String normalizedProductUrl = productUrl.trim();
        validateProductUrl(normalizedProductUrl);
        return normalizedProductUrl;
    }

    private void validateProductUrl(String productUrl) {
        URI uri;
        try {
            uri = new URI(productUrl);
        } catch (URISyntaxException exception) {
            throw new InvalidShopProductUrlException(productUrl);
        }

        String scheme = uri.getScheme();
        if ((!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || uri.getHost() == null
                || productUrl.length() > 1000) {
            throw new InvalidShopProductUrlException(productUrl);
        }
    }

    public static ShopProduct createCustom(
            Long childUserId,
            String title,
            String description,
            String imageUrl,
            String productUrl,
            int cloverPrice
    ) {
        return new ShopProduct(
                null,
                childUserId,
                title,
                description,
                imageUrl,
                productUrl,
                cloverPrice,
                null,
                ShopProductSource.CUSTOM,
                LocalDateTime.now()
        );
    }

    public static ShopProduct reconstitute(
            Long id,
            Long childUserId,
            String title,
            String description,
            String imageUrl,
            String productUrl,
            int cloverPrice,
            Integer purchasePriceWon,
            ShopProductSource source,
            LocalDateTime createdAt
    ) {
        return new ShopProduct(id, childUserId, title, description, imageUrl, productUrl, cloverPrice, purchasePriceWon, source, createdAt);
    }

    public static ShopProduct createFamily(Long childUserId, String title, String description,
            String imageUrl, String productUrl, int cloverPrice, Integer purchasePriceWon) {
        return new ShopProduct(null, childUserId, title, description, imageUrl, productUrl,
                cloverPrice, purchasePriceWon, ShopProductSource.FAMILY, LocalDateTime.now());
    }

    public ShopProduct price(int cloverPrice, Integer purchasePriceWon) {
        return new ShopProduct(id, childUserId, title, description, imageUrl, productUrl, cloverPrice, purchasePriceWon, source, createdAt);
    }

    public boolean isAvailableTo(Long requestingChildUserId) {
        return source == ShopProductSource.PRESET
                || (source != ShopProductSource.PRESET && childUserId != null && childUserId.equals(requestingChildUserId));
    }

    public Long getId() {
        return id;
    }

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
