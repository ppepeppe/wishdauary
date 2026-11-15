package com.wishdiary.adapter.in.web.shop;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.shop.CreateCatalogProductRequest;
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
        return CommonResponse.success("Catalog product created.", catalog.create(request));
    }
}
