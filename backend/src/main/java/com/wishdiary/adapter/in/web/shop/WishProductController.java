package com.wishdiary.adapter.in.web.shop;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.shop.CreateWishProductRequest;
import com.wishdiary.application.shop.ManageWishProductsUseCase;
import com.wishdiary.application.shop.ShopProductResponse;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop/wish-requests")
public class WishProductController {
    private final ManageWishProductsUseCase wishes;
    public WishProductController(ManageWishProductsUseCase wishes) { this.wishes = wishes; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<ShopProductResponse> create(@Authenticated AuthenticatedSession session,
            @Valid @RequestBody CreateWishProductRequest request) {
        return CommonResponse.success("Wish request sent.", wishes.requestWish(session.userId(), request));
    }
    @GetMapping
    public CommonResponse<List<ShopProductResponse>> list(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Wish requests retrieved.", wishes.getWishRequests(session.userId()));
    }
}
