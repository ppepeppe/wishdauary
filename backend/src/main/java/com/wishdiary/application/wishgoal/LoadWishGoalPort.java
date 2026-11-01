package com.wishdiary.application.wishgoal;

import com.wishdiary.domain.wishgoal.WishGoal;
import java.util.Optional;

public interface LoadWishGoalPort {

    Optional<WishGoal> loadByChildUserId(Long childUserId);
}
