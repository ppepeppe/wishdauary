package com.wishdiary.adapter.out.persistence.shop;

import com.wishdiary.application.shop.ArchiveCatalogProductPort;
import org.springframework.stereotype.Component;

@Component
public class CatalogArchiveAdapter implements ArchiveCatalogProductPort {
    private final ShopProductJpaRepository products;
    public CatalogArchiveAdapter(ShopProductJpaRepository products) { this.products = products; }
    @Override
    public void archive(Long productId) { products.archive(productId); }
}
