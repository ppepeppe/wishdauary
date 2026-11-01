package com.wishdiary.application.reaction;

import jakarta.validation.constraints.NotNull;

public record ToggleHeartReactionRequest(
        @NotNull Long diaryId
) {
}
