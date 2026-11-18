package com.wishdiary.adapter.in.web.familyprofile;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.familyprofile.ChildProfileResponse;
import com.wishdiary.application.familyprofile.GetChildProfileUseCase;
import com.wishdiary.common.response.CommonResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/family/children")
public class ChildProfileController {
    private final GetChildProfileUseCase profiles;
    public ChildProfileController(GetChildProfileUseCase profiles) { this.profiles = profiles; }

    @GetMapping("/{childId}")
    public CommonResponse<ChildProfileResponse> get(@Authenticated AuthenticatedSession session,
            @PathVariable Long childId) {
        return CommonResponse.success("Child profile retrieved.", profiles.get(session.userId(), childId));
    }
}
