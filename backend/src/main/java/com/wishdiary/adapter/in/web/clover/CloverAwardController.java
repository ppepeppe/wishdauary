package com.wishdiary.adapter.in.web.clover;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.clover.*;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clovers/awards")
public class CloverAwardController {
    private final AwardCloverUseCase awards;
    public CloverAwardController(AwardCloverUseCase awards) { this.awards = awards; }
    @PostMapping
    public CommonResponse<AwardCloverResponse> award(@Authenticated AuthenticatedSession session,
            @Valid @RequestBody AwardCloverRequest request) {
        return CommonResponse.success("Clovers awarded.", awards.award(session.userId(), request));
    }
}
