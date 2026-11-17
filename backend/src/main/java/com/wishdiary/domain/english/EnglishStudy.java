package com.wishdiary.domain.english;

import java.time.Instant;
import java.time.LocalDate;

public record EnglishStudy(Long id, Long childUserId, Long receiverFamilyUserId, LocalDate studyDate,
        String sentence, String meaning, Status status, Instant createdAt, Instant processedAt) {
    public enum Status { PENDING, APPROVED, REJECTED }
    public static EnglishStudy request(Long childId, Long familyId, LocalDate day, DailyEnglish.Line line, Instant now) {
        return new EnglishStudy(null, childId, familyId, day, line.sentence(), line.meaning(), Status.PENDING, now, null);
    }
    public EnglishStudy resubmit(Long familyId, Instant now) {
        if (status != Status.REJECTED) throw new InvalidStudyReviewException();
        return new EnglishStudy(id, childUserId, familyId, studyDate, sentence, meaning, Status.PENDING, now, null);
    }
    public EnglishStudy review(boolean approved, Instant now) {
        Status next = approved ? Status.APPROVED : Status.REJECTED;
        if (status == next) return this;
        if (status != Status.PENDING) throw new InvalidStudyReviewException();
        return new EnglishStudy(id, childUserId, receiverFamilyUserId, studyDate, sentence, meaning, next, createdAt, now);
    }
}
