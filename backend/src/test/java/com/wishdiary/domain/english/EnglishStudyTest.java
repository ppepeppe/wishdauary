package com.wishdiary.domain.english;

import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import org.junit.jupiter.api.Test;

class EnglishStudyTest {
    private final LocalDate date = LocalDate.of(2026, 11, 17);
    private final Instant now = Instant.parse("2026-11-17T03:00:00Z");
    @Test
    void contentIsDeterministicAndRotatesEveryDayAcrossThirtyDays() {
        assertEquals(DailyEnglish.forDate(date), DailyEnglish.forDate(date));
        assertNotEquals(DailyEnglish.forDate(date), DailyEnglish.forDate(date.plusDays(1)));
        assertEquals(DailyEnglish.forDate(date), DailyEnglish.forDate(date.plusDays(30)));
        assertEquals(30, date.datesUntil(date.plusDays(30)).map(DailyEnglish::forDate).distinct().count());
    }
    @Test
    void approvedStudyCannotBeReversedAndDuplicateApprovalKeepsTheOriginalTime() {
        var study = EnglishStudy.request(1L, 2L, date, DailyEnglish.forDate(date), now).review(true, now.plusSeconds(10));
        assertEquals(study, study.review(true, now.plusSeconds(20)));
        assertThrows(InvalidStudyReviewException.class, () -> study.review(false, now));
        assertThrows(InvalidStudyReviewException.class, () -> study.resubmit(3L, now));
    }
    @Test
    void retryingRejectedStudyPreservesTheStudiedContentAndDay() {
        var study = EnglishStudy.request(1L, 2L, date, DailyEnglish.forDate(date), now).review(false, now);
        var retry = study.resubmit(3L, now.plusSeconds(10));
        assertEquals(date, retry.studyDate()); assertEquals(study.sentence(), retry.sentence());
        assertEquals(3L, retry.receiverFamilyUserId()); assertEquals(EnglishStudy.Status.PENDING, retry.status());
        assertNull(retry.processedAt());
    }
}
