package com.wishdiary.application.wishgoal;

import com.wishdiary.domain.wishgoal.WishGoal;

public interface SaveWishGoalPort {

    WishGoal save(WishGoal wishGoal);

    void deleteByChildUserId(Long childUserId);
}
