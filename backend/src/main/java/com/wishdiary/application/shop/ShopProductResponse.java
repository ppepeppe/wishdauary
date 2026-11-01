package com.wishdiary.application.shop;

import java.time.LocalDateTime;

public record ShopProductResponse(
        Long productId,
        String title,
        String description,
        String imageUrl,
        String productUrl,
        int cloverPrice,
        Integer purchasePriceWon,
        String source,
        Long childUserId,
        LocalDateTime createdAt
) {
}
