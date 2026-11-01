package com.wishdiary.application.shop;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShopProductRequest(
        @NotBlank String title,
        @NotBlank String description,
        String imageUrl,
        @Size(max = 1000)
        String productUrl,
        @Min(0) Integer cloverPrice
) {
}
