package com.wishdiary.application.diary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FamilyDiaryFeedResponse(
        Long diaryId,
        Long authorId,
        String authorNickname,
        String authorRelationLabel,
        String contentPreview,
        String emotion,
        boolean hearted,
        long heartCount,
        long commentCount,
        LocalDate writtenDate,
        LocalDateTime createdAt,
        String visibilityScope
) {
}
