package com.wishdiary.application.diary;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

public record UpdateDiaryRequest(
        @NotBlank String content,
        String emotion,
        @NotBlank String visibilityScope,
        List<Long> sharingGroupIds
) {
    public UpdateDiaryRequest(String content, String emotion, String visibilityScope) {
        this(content, emotion, visibilityScope, List.of());
    }
}
