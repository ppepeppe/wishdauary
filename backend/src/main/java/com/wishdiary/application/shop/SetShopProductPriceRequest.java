package com.wishdiary.application.shop;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SetShopProductPriceRequest(
        @NotNull @Min(1) Integer cloverPrice,
        @Min(0) Integer purchasePriceWon
) {
}
