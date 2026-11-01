package com.wishdiary.application.reaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDiaryCommentRequest(
        @NotNull Long diaryId,
        @NotBlank String content
) {
}
