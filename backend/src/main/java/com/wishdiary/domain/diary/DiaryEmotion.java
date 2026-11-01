package com.wishdiary.domain.diary;

public enum DiaryEmotion {
    HAPPY,
    SAD,
    ANGRY,
    EXCITED,
    TIRED;

    public static DiaryEmotion from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return DiaryEmotion.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidDiaryEmotionException();
        }
    }
}
