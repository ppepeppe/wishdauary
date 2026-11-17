package com.wishdiary.adapter.in.web.english;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.english.*;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/english/contents")
public class AdminEnglishContentController {
    private final ManageEnglishContentsUseCase contents;
    public AdminEnglishContentController(ManageEnglishContentsUseCase contents) { this.contents = contents; }
    // Match the catalog: login is required, ADMIN authorization remains intentionally deferred.
    @GetMapping
    public CommonResponse<EnglishContentOverview> list(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("English contents retrieved.", contents.list());
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<EnglishContentResponse> create(@Authenticated AuthenticatedSession session,
            @Valid @RequestBody CreateEnglishContentRequest request) {
        return CommonResponse.success("English content created.", contents.create(request));
    }
    @PatchMapping("/{contentId}")
    public CommonResponse<EnglishContentResponse> update(@Authenticated AuthenticatedSession session,
            @PathVariable Long contentId, @Valid @RequestBody UpdateEnglishContentRequest request) {
        return CommonResponse.success("English content updated.", contents.update(contentId, request));
    }
}
