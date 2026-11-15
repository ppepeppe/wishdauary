package com.wishdiary.application.shop;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCatalogProductRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 500) String description,
        @Size(max = 700_000, message = "사진 용량을 확인해 주세요.") String imageData,
        @Size(max = 1000) String productUrl,
        @NotNull @Min(1) Integer cloverPrice,
        @Min(0) Integer purchasePriceWon
) {
}
