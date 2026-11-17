package com.wishdiary.adapter.in.web.english;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.english.*;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/english")
public class EnglishStudyController {
    public record RequestStudy(@NotNull @Positive Long receiverFamilyUserId, @NotNull java.time.LocalDate studyDate, Long contentId, Long contentVersion) {}
    public record ReviewStudy(@NotNull Action action) {}
    public enum Action { APPROVE, REJECT }
    private final ManageEnglishStudyUseCase studies;
    public EnglishStudyController(ManageEnglishStudyUseCase studies) { this.studies = studies; }
    @GetMapping("/today")
    public CommonResponse<EnglishTodayResponse> today(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Today's English retrieved.", studies.today(session.userId()));
    }
    @PostMapping("/requests")
    public CommonResponse<EnglishStudyResponse> request(@Authenticated AuthenticatedSession session, @Valid @RequestBody RequestStudy body) {
        return CommonResponse.success("English confirmation requested.", studies.request(session.userId(), body.receiverFamilyUserId(), body.studyDate(), body.contentId(), body.contentVersion()));
    }
    @GetMapping("/requests")
    public CommonResponse<List<EnglishStudyResponse>> pending(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("English requests retrieved.", studies.pending(session.userId()));
    }
    @PatchMapping("/requests/{requestId}")
    public CommonResponse<EnglishStudyResponse> review(@Authenticated AuthenticatedSession session,
            @PathVariable Long requestId, @Valid @RequestBody ReviewStudy body) {
        return CommonResponse.success("English request reviewed.", studies.review(session.userId(), requestId, body.action() == Action.APPROVE));
    }
}
