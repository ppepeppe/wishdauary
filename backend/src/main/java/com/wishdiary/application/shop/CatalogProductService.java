package com.wishdiary.application.shop;

import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductSource;
import com.wishdiary.domain.shop.ShopProductNotFoundException;
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
    private final ArchiveCatalogProductPort archive;

    public CatalogProductService(LoadShopProductPort products, SaveShopProductPort saveProducts,
            DecodeShopPhotoPort photos, ShopProductResponseMapper mapper, ArchiveCatalogProductPort archive) {
        this.products = products;
        this.saveProducts = saveProducts;
        this.photos = photos;
        this.mapper = mapper;
        this.archive = archive;
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
    public ShopProductResponse create(Long userId, CreateCatalogProductRequest request) {
        ShopProduct product = ShopProduct.createPreset(request.title().trim(), request.description().trim(),
                request.productUrl(), request.cloverPrice(), request.purchasePriceWon())
                .withPhoto(photos.decode(request.imageData())).withCreator(userId);
        return mapper.toResponse(saveProducts.save(product));
    }

    @Override
    @Transactional
    public ShopProductResponse updatePrice(Long productId, UpdateCatalogPriceRequest request) {
        ShopProduct product = requireCatalogProduct(productId);
        return mapper.toResponse(saveProducts.save(product.price(request.cloverPrice().intValueExact(), product.getPurchasePriceWon())));
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        requireCatalogProduct(productId);
        archive.archive(productId);
    }

    private ShopProduct requireCatalogProduct(Long id) {
        return products.loadForUpdate(id).filter(product -> product.getSource() == ShopProductSource.PRESET)
                .orElseThrow(() -> new ShopProductNotFoundException(id));
    }
}
