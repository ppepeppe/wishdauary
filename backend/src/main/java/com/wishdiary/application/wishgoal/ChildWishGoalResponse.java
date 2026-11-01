package com.wishdiary.application.wishgoal;

public record ChildWishGoalResponse(
        Long childUserId,
        String childNickname,
        WishGoalResponse goal
) {
}
