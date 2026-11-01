package com.wishdiary.adapter.in.web.user;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.user.GetHomeSummaryUseCase;
import com.wishdiary.application.user.GetMyProfileUseCase;
import com.wishdiary.application.user.GetUserStreakUseCase;
import com.wishdiary.application.user.HomeSummaryResponse;
import com.wishdiary.application.user.MyProfileResponse;
import com.wishdiary.application.user.UpdateMyProfileRequest;
import com.wishdiary.application.user.UpdateMyProfileUseCase;
import com.wishdiary.application.user.UserStreakResponse;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetHomeSummaryUseCase getHomeSummaryUseCase;
    private final GetUserStreakUseCase getUserStreakUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;

    public UserController(
            GetHomeSummaryUseCase getHomeSummaryUseCase,
            GetUserStreakUseCase getUserStreakUseCase,
            GetMyProfileUseCase getMyProfileUseCase,
            UpdateMyProfileUseCase updateMyProfileUseCase
    ) {
        this.getHomeSummaryUseCase = getHomeSummaryUseCase;
        this.getUserStreakUseCase = getUserStreakUseCase;
        this.getMyProfileUseCase = getMyProfileUseCase;
        this.updateMyProfileUseCase = updateMyProfileUseCase;
    }

    @GetMapping("/me")
    public CommonResponse<MyProfileResponse> getMyProfile(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("My profile retrieved.", getMyProfileUseCase.get(session.userId()));
    }

    @PatchMapping("/me")
    public CommonResponse<MyProfileResponse> updateMyProfile(
            @Authenticated AuthenticatedSession session,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return CommonResponse.success("My profile updated.", updateMyProfileUseCase.update(session.userId(), request));
    }

    @GetMapping("/home-summary")
    public CommonResponse<HomeSummaryResponse> getHomeSummary(
            @Authenticated AuthenticatedSession session,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        HomeSummaryResponse response = getHomeSummaryUseCase.getSummary(session.userId(), date);
        return CommonResponse.success("Home summary retrieved.", response);
    }

    @GetMapping("/streak")
    public CommonResponse<UserStreakResponse> getStreak(@Authenticated AuthenticatedSession session) {
        UserStreakResponse response = getUserStreakUseCase.getStreak(session.userId());
        return CommonResponse.success("User streak retrieved.", response);
    }
}
