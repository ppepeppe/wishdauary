package com.wishdiary.application.wishgoal;

import java.util.List;

public interface GetChildrenWishGoalsUseCase {

    List<ChildWishGoalResponse> getChildrenGoals(Long familyUserId);
}
