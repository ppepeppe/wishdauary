package com.wishdiary.domain.user;

import java.util.Locale;
import java.util.Set;

public record ProfileAvatar(
        String preset,
        String imageDataUrl,
        String character
) {

    public static final String DEFAULT_CHARACTER = "my-melody";
    public static final Set<String> CHARACTERS = Set.of("my-melody", "kuromi", "cinnamoroll");

    public static final String DEFAULT_PRESET = "peach";
    public static final int MAX_IMAGE_CHARS = 300_000;
    public static final Set<String> PRESETS = Set.of(
            "peach",
            "mint",
            "sky",
            "lavender",
            "honey",
            "rose"
    );

    private static final String DATA_IMAGE_PREFIX = "data:image/";

    public ProfileAvatar(String preset, String imageDataUrl) {
        this(preset, imageDataUrl, DEFAULT_CHARACTER);
    }

    public static ProfileAvatar defaultAvatar() {
        return new ProfileAvatar(DEFAULT_PRESET, null);
    }

    public static ProfileAvatar of(String preset, String imageDataUrl) {
        return of(preset, imageDataUrl, DEFAULT_CHARACTER);
    }

    public static ProfileAvatar of(String preset, String imageDataUrl, String character) {
        return new ProfileAvatar(normalizePreset(preset), normalizeImage(imageDataUrl), normalizeCharacter(character));
    }

    private static String normalizeCharacter(String character) {
        if (character == null || character.isBlank()) return DEFAULT_CHARACTER;
        String normalized = character.trim().toLowerCase(Locale.ROOT);
        if (!CHARACTERS.contains(normalized)) throw new InvalidProfileAvatarException();
        return normalized;
    }

    public boolean hasCustomImage() {
        return imageDataUrl != null && !imageDataUrl.isBlank();
    }

    private static String normalizePreset(String preset) {
        if (preset == null || preset.isBlank()) {
            return DEFAULT_PRESET;
        }

        String normalized = preset.trim().toLowerCase(Locale.ROOT);
        if (!PRESETS.contains(normalized)) {
            throw new InvalidProfileAvatarException();
        }
        return normalized;
    }

    private static String normalizeImage(String imageDataUrl) {
        if (imageDataUrl == null || imageDataUrl.isBlank()) {
            return null;
        }

        String trimmed = imageDataUrl.trim();
        if (!trimmed.startsWith(DATA_IMAGE_PREFIX) || !trimmed.contains(";base64,")) {
            throw new InvalidProfileAvatarException();
        }
        if (trimmed.length() > MAX_IMAGE_CHARS) {
            throw new InvalidProfileAvatarException();
        }
        return trimmed;
    }
}
