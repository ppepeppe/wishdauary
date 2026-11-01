package com.wishdiary.application.wishgoal;

import jakarta.validation.constraints.NotNull;

public record SetWishGoalRequest(
        @NotNull Long productId
) {
}
