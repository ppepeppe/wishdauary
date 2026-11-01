package com.wishdiary.application.shop;

import com.wishdiary.domain.shop.ShopProduct;
import org.springframework.stereotype.Component;

@Component
public class ShopProductResponseMapper {

    public ShopProductResponse toResponse(ShopProduct product) {
        return new ShopProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getImageUrl(),
                product.getProductUrl(),
                product.getCloverPrice(),
                product.getPurchasePriceWon(),
                product.getSource().name().toLowerCase(),
                product.getChildUserId(),
                product.getCreatedAt()
        );
    }
}
