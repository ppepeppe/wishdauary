package com.wishdiary.application.shop;

import java.util.List;

public interface ManageWishProductsUseCase {
    ShopProductResponse requestWish(Long childUserId, CreateWishProductRequest request);
    List<ShopProductResponse> getWishRequests(Long childUserId);
}
