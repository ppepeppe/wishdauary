package com.wishdiary.application.shop;

public interface CreateShopProductUseCase {

    ShopProductResponse create(Long childUserId, CreateShopProductRequest request);
}
