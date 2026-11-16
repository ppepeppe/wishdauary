package com.wishdiary.adapter.in.web.shop;

import com.wishdiary.application.shop.ConfirmGiftReceiptUseCase;
import com.wishdiary.application.shop.GiftRequestNotificationResponse;
import com.wishdiary.common.response.CommonResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;

@RestController
public class GiftReceiptController {
    private final ConfirmGiftReceiptUseCase receipts;

    public GiftReceiptController(ConfirmGiftReceiptUseCase receipts) { this.receipts = receipts; }

    @PatchMapping("/api/v1/shop/gift-requests/{requestId}/receipt")
    public CommonResponse<GiftRequestNotificationResponse> confirmReceipt(
            @Authenticated AuthenticatedSession session, @PathVariable Long requestId) {
        return CommonResponse.success("Gift receipt confirmed.", receipts.confirmReceipt(session.userId(), requestId));
    }
}
