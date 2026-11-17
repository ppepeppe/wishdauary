package com.wishdiary.application.shop;

import java.util.List;

public interface ManageCatalogProductsUseCase {
    List<ShopProductResponse> list();

    ShopProductResponse create(Long userId, CreateCatalogProductRequest request);

    ShopProductResponse updatePrice(Long productId, UpdateCatalogPriceRequest request);

    void delete(Long productId);
}
