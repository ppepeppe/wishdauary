package com.wishdiary.application.diary;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateDiaryResponse(
        Long diaryId,
        Long authorId,
        String content,
        String emotion,
        String visibilityScope,
        LocalDate writtenDate,
        LocalDateTime createdAt,
        List<Long> sharingGroupIds
) {
}
