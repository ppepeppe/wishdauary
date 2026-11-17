package com.wishdiary.domain.english;

public record EnglishLesson(Long contentId, Long version, String sentence, String meaning) {
    public static EnglishLesson snapshot(EnglishStudy study) {
        return new EnglishLesson(null, null, study.sentence(), study.meaning());
    }
}
