package com.wishdiary.application.diary;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDiaryRequest(
        @NotBlank String content,
        String emotion,
        @NotBlank String visibilityScope,
        List<Long> sharingGroupIds,
        @Size(max = 700000) String imageData,
        boolean removePhoto
) {
    public UpdateDiaryRequest(String content, String emotion, String visibilityScope, List<Long> sharingGroupIds) {
        this(content, emotion, visibilityScope, sharingGroupIds, null, false);
    }
    public UpdateDiaryRequest(String content, String emotion, String visibilityScope) {
        this(content, emotion, visibilityScope, List.of(), null, false);
    }
}
