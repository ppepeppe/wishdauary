package com.wishdiary.application.shop;

import com.wishdiary.domain.shop.ShopProduct;
import java.util.List;
import java.util.Optional;

public interface LoadShopProductPort {

    List<ShopProduct> loadAll();

    Optional<ShopProduct> loadById(Long productId);

}
