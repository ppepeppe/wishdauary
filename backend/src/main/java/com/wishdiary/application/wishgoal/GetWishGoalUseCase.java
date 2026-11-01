package com.wishdiary.application.wishgoal;

public interface GetWishGoalUseCase {

    WishGoalResponse getGoal(Long childUserId);
}
