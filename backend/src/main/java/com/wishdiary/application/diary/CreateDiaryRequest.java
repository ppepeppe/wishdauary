package com.wishdiary.application.diary;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateDiaryRequest(
        @NotBlank String content,
        String emotion,
        @NotBlank String visibilityScope,
        @NotNull LocalDate writtenDate,
        List<Long> sharingGroupIds
) {
    public CreateDiaryRequest(String content, String emotion, String visibilityScope, LocalDate writtenDate) {
        this(content, emotion, visibilityScope, writtenDate, List.of());
    }
}
