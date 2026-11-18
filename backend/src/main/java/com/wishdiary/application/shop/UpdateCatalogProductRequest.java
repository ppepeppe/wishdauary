package com.wishdiary.application.shop;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateCatalogProductRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 500) String description,
        @Size(max = 700_000, message = "사진 용량을 확인해 주세요.") String imageData,
        boolean removeImage,
        @Size(max = 1000) String productUrl,
        @NotNull @Min(1) @Max(2147483647) @Digits(integer = 10, fraction = 0) BigDecimal cloverPrice,
        @Min(0) @Max(2147483647) @Digits(integer = 10, fraction = 0) BigDecimal purchasePriceWon
) { }
