package com.wishdiary.application.shop;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateCatalogPriceRequest(
        @NotNull @Min(1) @Max(2147483647) @Digits(integer = 10, fraction = 0) BigDecimal cloverPrice) {}
