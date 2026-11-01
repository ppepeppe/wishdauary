package com.wishdiary.application.shop;

import java.util.List;

public interface GetShopProductsUseCase {

    List<ShopProductResponse> getProducts(Long childUserId);
}
