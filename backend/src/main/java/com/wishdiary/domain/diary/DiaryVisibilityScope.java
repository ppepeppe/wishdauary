package com.wishdiary.domain.diary;

public enum DiaryVisibilityScope {
    PRIVATE,
    FAMILY,
    FRIENDS,
    PUBLIC,
    GROUP;

    public static DiaryVisibilityScope from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDiaryVisibilityScopeException();
        }

        try {
            return DiaryVisibilityScope.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidDiaryVisibilityScopeException();
        }
    }
}

