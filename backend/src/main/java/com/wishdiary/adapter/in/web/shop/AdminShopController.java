package com.wishdiary.adapter.in.web.shop;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.shop.CreateCatalogProductRequest;
import com.wishdiary.application.shop.UpdateCatalogPriceRequest;
import com.wishdiary.application.shop.UpdateCatalogProductRequest;
import com.wishdiary.application.shop.ManageCatalogProductsUseCase;
import com.wishdiary.application.shop.ShopProductResponse;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/shop/products")
public class AdminShopController {
    private final ManageCatalogProductsUseCase catalog;

    public AdminShopController(ManageCatalogProductsUseCase catalog) { this.catalog = catalog; }

    // Keep existing login authentication. ADMIN authorization is intentionally deferred.
    @GetMapping
    public CommonResponse<List<ShopProductResponse>> list(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Catalog products retrieved.", catalog.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<ShopProductResponse> create(@Authenticated AuthenticatedSession session,
            @Valid @RequestBody CreateCatalogProductRequest request) {
        return CommonResponse.success("Catalog product created.", catalog.create(session.userId(), request));
    }

    @PatchMapping("/{productId}")
    public CommonResponse<ShopProductResponse> updatePrice(@Authenticated AuthenticatedSession session,
            @PathVariable Long productId, @Valid @RequestBody UpdateCatalogPriceRequest request) {
        return CommonResponse.success("Catalog price updated.", catalog.updatePrice(productId, request));
    }

    @PutMapping("/{productId}")
    public CommonResponse<ShopProductResponse> update(@Authenticated AuthenticatedSession session,
            @PathVariable Long productId, @Valid @RequestBody UpdateCatalogProductRequest request) {
        return CommonResponse.success("Catalog product updated.", catalog.update(productId, request));
    }

    @DeleteMapping("/{productId}")
    public CommonResponse<Void> delete(@Authenticated AuthenticatedSession session, @PathVariable Long productId) {
        catalog.delete(productId);
        return CommonResponse.success("Catalog product deleted.", null);
    }
}
