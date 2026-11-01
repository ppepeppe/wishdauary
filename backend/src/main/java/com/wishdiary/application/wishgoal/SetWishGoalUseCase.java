package com.wishdiary.application.wishgoal;

public interface SetWishGoalUseCase {

    WishGoalResponse setGoal(Long childUserId, SetWishGoalRequest request);
}
