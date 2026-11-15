package com.wishdiary.application.shop;

import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductSource;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogProductService implements ManageCatalogProductsUseCase {
    private final LoadShopProductPort products;
    private final SaveShopProductPort saveProducts;
    private final DecodeShopPhotoPort photos;
    private final ShopProductResponseMapper mapper;

    public CatalogProductService(LoadShopProductPort products, SaveShopProductPort saveProducts,
            DecodeShopPhotoPort photos, ShopProductResponseMapper mapper) {
        this.products = products;
        this.saveProducts = saveProducts;
        this.photos = photos;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopProductResponse> list() {
        return products.loadAll().stream()
                .filter(product -> product.getSource() == ShopProductSource.PRESET)
                .sorted(Comparator.comparing(ShopProduct::getCreatedAt).reversed().thenComparing(ShopProduct::getId))
                .map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ShopProductResponse create(CreateCatalogProductRequest request) {
        ShopProduct product = ShopProduct.createPreset(request.title().trim(), request.description().trim(),
                request.productUrl(), request.cloverPrice(), request.purchasePriceWon())
                .withPhoto(photos.decode(request.imageData()));
        return mapper.toResponse(saveProducts.save(product));
    }
}
