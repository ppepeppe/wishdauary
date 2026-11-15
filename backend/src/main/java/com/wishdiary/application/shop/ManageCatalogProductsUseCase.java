package com.wishdiary.application.shop;

import java.util.List;

public interface ManageCatalogProductsUseCase {
    List<ShopProductResponse> list();

    ShopProductResponse create(CreateCatalogProductRequest request);
}
