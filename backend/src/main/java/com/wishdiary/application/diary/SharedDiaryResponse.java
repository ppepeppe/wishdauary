package com.wishdiary.application.diary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SharedDiaryResponse(Long diaryId, String authorNickname, String contentPreview,
        String emotion, String visibilityScope, LocalDate writtenDate, LocalDateTime createdAt) { }
