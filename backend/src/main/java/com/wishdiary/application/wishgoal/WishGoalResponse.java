package com.wishdiary.application.wishgoal;

import java.time.LocalDateTime;

public record WishGoalResponse(
        Long goalId,
        Long productId,
        String productTitle,
        String productImageUrl,
        int targetCloverPrice,
        int currentCloverBalance,
        int remainingClovers,
        LocalDateTime createdAt
) {
}
