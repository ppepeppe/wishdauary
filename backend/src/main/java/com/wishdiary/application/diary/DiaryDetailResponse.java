package com.wishdiary.application.diary;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryDetailResponse(
        Long diaryId,
        Long authorId,
        String content,
        String emotion,
        String visibilityScope,
        long heartCount,
        long commentCount,
        LocalDate writtenDate,
        LocalDateTime createdAt,
        List<Long> sharingGroupIds
) {
}
