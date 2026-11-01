package com.wishdiary.application.diary;

public record TodayDiaryExistenceResponse(
        boolean exists,
        long count,
        Long diaryId,
        String emotion,
        long heartCount,
        long commentCount
) {
}
