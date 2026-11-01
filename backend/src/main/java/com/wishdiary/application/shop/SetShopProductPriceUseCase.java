package com.wishdiary.application.shop;

public interface SetShopProductPriceUseCase {

    ShopProductResponse setPrice(Long familyUserId, Long productId, SetShopProductPriceRequest request);
}
