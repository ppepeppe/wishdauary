package com.wishdiary.application.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestGiftRequest(
        @NotNull Long productId,
        @NotNull Long receiverFamilyUserId,
        @NotBlank String message
) {
}
