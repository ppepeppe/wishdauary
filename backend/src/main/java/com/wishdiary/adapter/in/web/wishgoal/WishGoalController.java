package com.wishdiary.adapter.in.web.wishgoal;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.wishgoal.ChildWishGoalResponse;
import com.wishdiary.application.wishgoal.ClearWishGoalUseCase;
import com.wishdiary.application.wishgoal.GetChildrenWishGoalsUseCase;
import com.wishdiary.application.wishgoal.GetWishGoalUseCase;
import com.wishdiary.application.wishgoal.SetWishGoalRequest;
import com.wishdiary.application.wishgoal.SetWishGoalUseCase;
import com.wishdiary.application.wishgoal.WishGoalResponse;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wish-goals")
public class WishGoalController {

    private final SetWishGoalUseCase setWishGoalUseCase;
    private final GetWishGoalUseCase getWishGoalUseCase;
    private final ClearWishGoalUseCase clearWishGoalUseCase;
    private final GetChildrenWishGoalsUseCase getChildrenWishGoalsUseCase;

    public WishGoalController(
            SetWishGoalUseCase setWishGoalUseCase,
            GetWishGoalUseCase getWishGoalUseCase,
            ClearWishGoalUseCase clearWishGoalUseCase,
            GetChildrenWishGoalsUseCase getChildrenWishGoalsUseCase
    ) {
        this.setWishGoalUseCase = setWishGoalUseCase;
        this.getWishGoalUseCase = getWishGoalUseCase;
        this.clearWishGoalUseCase = clearWishGoalUseCase;
        this.getChildrenWishGoalsUseCase = getChildrenWishGoalsUseCase;
    }

    @PutMapping("/me")
    public CommonResponse<WishGoalResponse> setGoal(
            @Authenticated AuthenticatedSession session,
            @Valid @RequestBody SetWishGoalRequest request
    ) {
        return CommonResponse.success("Wish goal set.", setWishGoalUseCase.setGoal(session.userId(), request));
    }

    @GetMapping("/me")
    public CommonResponse<WishGoalResponse> getGoal(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Wish goal retrieved.", getWishGoalUseCase.getGoal(session.userId()));
    }

    @DeleteMapping("/me")
    public CommonResponse<Void> clearGoal(@Authenticated AuthenticatedSession session) {
        clearWishGoalUseCase.clearGoal(session.userId());
        return CommonResponse.success("Wish goal cleared.", null);
    }

    @GetMapping("/children")
    public CommonResponse<List<ChildWishGoalResponse>> getChildrenGoals(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Children wish goals retrieved.", getChildrenWishGoalsUseCase.getChildrenGoals(session.userId()));
    }
}
