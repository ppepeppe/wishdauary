package com.wishdiary.application.english;

import com.wishdiary.domain.english.EnglishContent;
import java.time.Instant;

public record EnglishContentResponse(Long contentId, String sentence, String meaning, long version,
        Instant createdAt, Instant updatedAt) {
    public static EnglishContentResponse from(EnglishContent content) {
        return new EnglishContentResponse(content.id(), content.sentence(), content.meaning(), content.version(),
                content.createdAt(), content.updatedAt());
    }
}
