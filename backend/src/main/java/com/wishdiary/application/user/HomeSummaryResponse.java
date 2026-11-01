package com.wishdiary.application.user;

public record HomeSummaryResponse(
        boolean hasTodayDiary,
        Long todayDiaryId,
        String todayDiaryEmotion,
        long todayDiaryCount,
        long todayDiaryHeartCount,
        long todayDiaryCommentCount,
        int cloverBalance,
        int currentStreak,
        int maxStreak
) {
}
