package com.wishdiary.adapter.in.web.shop;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.shop.CreateShopProductRequest;
import com.wishdiary.application.shop.CreateShopProductUseCase;
import com.wishdiary.application.shop.GetShopFamilyRecipientsUseCase;
import com.wishdiary.application.shop.GetShopProductsUseCase;
import com.wishdiary.application.shop.GetShopProductPricingRequestsUseCase;
import com.wishdiary.application.shop.GetSentGiftRequestsUseCase;
import com.wishdiary.application.shop.GetGiftRequestsUseCase;
import com.wishdiary.application.shop.GiftRequestNotificationResponse;
import com.wishdiary.application.shop.GiftRequestResponse;
import com.wishdiary.application.shop.ProcessGiftRequestRequest;
import com.wishdiary.application.shop.ProcessGiftRequestUseCase;
import com.wishdiary.application.shop.RequestGiftRequest;
import com.wishdiary.application.shop.RequestGiftUseCase;
import com.wishdiary.application.shop.SetShopProductPriceRequest;
import com.wishdiary.application.shop.SetShopProductPriceUseCase;
import com.wishdiary.application.shop.ShopFamilyRecipientResponse;
import com.wishdiary.application.shop.ShopProductResponse;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shop")
public class ShopController {

    private final GetShopProductsUseCase getShopProductsUseCase;
    private final GetShopFamilyRecipientsUseCase getShopFamilyRecipientsUseCase;
    private final CreateShopProductUseCase createShopProductUseCase;
    private final RequestGiftUseCase requestGiftUseCase;
    private final GetGiftRequestsUseCase getGiftRequestsUseCase;
    private final GetSentGiftRequestsUseCase getSentGiftRequestsUseCase;
    private final ProcessGiftRequestUseCase processGiftRequestUseCase;
    private final GetShopProductPricingRequestsUseCase getShopProductPricingRequestsUseCase;
    private final SetShopProductPriceUseCase setShopProductPriceUseCase;

    public ShopController(
            GetShopProductsUseCase getShopProductsUseCase,
            GetShopFamilyRecipientsUseCase getShopFamilyRecipientsUseCase,
            CreateShopProductUseCase createShopProductUseCase,
            RequestGiftUseCase requestGiftUseCase,
            GetGiftRequestsUseCase getGiftRequestsUseCase,
            GetSentGiftRequestsUseCase getSentGiftRequestsUseCase,
            ProcessGiftRequestUseCase processGiftRequestUseCase,
            GetShopProductPricingRequestsUseCase getShopProductPricingRequestsUseCase,
            SetShopProductPriceUseCase setShopProductPriceUseCase
    ) {
        this.getShopProductsUseCase = getShopProductsUseCase;
        this.getShopFamilyRecipientsUseCase = getShopFamilyRecipientsUseCase;
        this.createShopProductUseCase = createShopProductUseCase;
        this.requestGiftUseCase = requestGiftUseCase;
        this.getGiftRequestsUseCase = getGiftRequestsUseCase;
        this.getSentGiftRequestsUseCase = getSentGiftRequestsUseCase;
        this.processGiftRequestUseCase = processGiftRequestUseCase;
        this.getShopProductPricingRequestsUseCase = getShopProductPricingRequestsUseCase;
        this.setShopProductPriceUseCase = setShopProductPriceUseCase;
    }

    @GetMapping("/products")
    public CommonResponse<List<ShopProductResponse>> getProducts(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Shop products retrieved.", getShopProductsUseCase.getProducts(session.userId()));
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<ShopProductResponse> createProduct(
            @Authenticated AuthenticatedSession session,
            @Valid @RequestBody CreateShopProductRequest request
    ) {
        return CommonResponse.success("Shop product created.", createShopProductUseCase.create(session.userId(), request));
    }

    @GetMapping("/recipients")
    public CommonResponse<List<ShopFamilyRecipientResponse>> getRecipients(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Shop recipients retrieved.", getShopFamilyRecipientsUseCase.getRecipients(session.userId()));
    }

    @GetMapping("/product-pricing-requests")
    public CommonResponse<List<ShopProductResponse>> getProductPricingRequests(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Shop product pricing requests retrieved.", getShopProductPricingRequestsUseCase.getPricingRequests(session.userId()));
    }

    @PatchMapping("/products/{productId}/price")
    public CommonResponse<ShopProductResponse> setProductPrice(
            @Authenticated AuthenticatedSession session,
            @PathVariable Long productId,
            @Valid @RequestBody SetShopProductPriceRequest request
    ) {
        return CommonResponse.success("Shop product price set.", setShopProductPriceUseCase.setPrice(session.userId(), productId, request));
    }

    @PostMapping("/gift-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<GiftRequestResponse> requestGift(
            @Authenticated AuthenticatedSession session,
            @Valid @RequestBody RequestGiftRequest request
    ) {
        return CommonResponse.success("Gift request created.", requestGiftUseCase.request(session.userId(), request));
    }

    @GetMapping("/gift-requests")
    public CommonResponse<List<GiftRequestNotificationResponse>> getGiftRequests(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Gift requests retrieved.", getGiftRequestsUseCase.getReceivedRequests(session.userId()));
    }

    @GetMapping("/gift-requests/sent")
    public CommonResponse<List<GiftRequestNotificationResponse>> getSentGiftRequests(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Sent gift requests retrieved.", getSentGiftRequestsUseCase.getSentRequests(session.userId()));
    }

    @PatchMapping("/gift-requests/{requestId}")
    public CommonResponse<GiftRequestNotificationResponse> processGiftRequest(
            @Authenticated AuthenticatedSession session,
            @PathVariable Long requestId,
            @Valid @RequestBody ProcessGiftRequestRequest request
    ) {
        return CommonResponse.success("Gift request processed.", processGiftRequestUseCase.process(session.userId(), requestId, request));
    }
}
