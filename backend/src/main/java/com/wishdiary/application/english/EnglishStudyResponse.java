package com.wishdiary.application.english;

import java.time.Instant;
import java.time.LocalDate;

public record EnglishStudyResponse(Long requestId, Long childUserId, String childNickname,
        Long receiverFamilyUserId, String receiverName, LocalDate studyDate, String sentence, String meaning,
        String status, Instant createdAt, Instant processedAt) {}
