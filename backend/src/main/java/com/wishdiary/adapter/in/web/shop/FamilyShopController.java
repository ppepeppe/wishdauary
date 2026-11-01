package com.wishdiary.adapter.in.web.shop;
import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.shop.*;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop")
public class FamilyShopController {
    private final ManageFamilyShopUseCase shop;
    public FamilyShopController(ManageFamilyShopUseCase shop) { this.shop = shop; }
    @GetMapping("/family-products")
    public CommonResponse<List<ShopProductResponse>> list(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Family products retrieved.", shop.list(session.userId()));
    }
    @GetMapping("/managed-products")
    public CommonResponse<List<ShopProductResponse>> listManaged(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Managed products retrieved.", shop.listManaged(session.userId()));
    }
    @PostMapping("/family-products") @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<ShopProductResponse> create(@Authenticated AuthenticatedSession session,
            @Valid @RequestBody CreateFamilyShopProductRequest request) {
        return CommonResponse.success("Family product created.", shop.create(session.userId(), request));
    }
}
