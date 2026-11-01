package com.wishdiary.application.diary;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyDiarySummaryResponse(
        Long diaryId,
        String contentPreview,
        String emotion,
        String visibilityScope,
        long heartCount,
        long commentCount,
        LocalDate writtenDate,
        LocalDateTime createdAt,
        List<Long> sharingGroupIds
) {
}
