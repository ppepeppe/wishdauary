package com.wishdiary.application.clover;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record AwardCloverRequest(@NotNull @Positive Long childUserId,
        @NotNull @Min(1) @Max(1000) @Digits(integer = 4, fraction = 0) BigDecimal amount,
        @NotBlank @Size(max = 300) String reason, @NotNull UUID requestKey) {}
