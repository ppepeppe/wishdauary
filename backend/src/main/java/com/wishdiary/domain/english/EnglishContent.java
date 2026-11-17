package com.wishdiary.domain.english;

import java.time.Instant;

public record EnglishContent(Long id, String sentence, String meaning, long version, Instant createdAt, Instant updatedAt) {
    public static EnglishContent create(String sentence, String meaning, Instant now) {
        return new EnglishContent(null, sentence.trim(), meaning.trim(), 0, now, now);
    }
    public EnglishContent revise(String sentence, String meaning, Instant now) {
        return new EnglishContent(id, sentence.trim(), meaning.trim(), version, createdAt, now);
    }
    public EnglishLesson lesson() { return new EnglishLesson(id, version, sentence, meaning); }
}
