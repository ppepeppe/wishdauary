package com.wishdiary.application.shop;

import jakarta.validation.constraints.NotNull;

public record ProcessGiftRequestRequest(
        @NotNull ProcessGiftRequestAction action
) {
}
