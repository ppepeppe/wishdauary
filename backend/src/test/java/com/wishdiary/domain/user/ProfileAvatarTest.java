package com.wishdiary.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProfileAvatarTest {

    @Test
    void characterAndBackgroundCanBeChosenIndependently() {
        for (String character : new String[]{"my-melody", "kuromi", "cinnamoroll"}) {
            for (String color : ProfileAvatar.PRESETS) {
                ProfileAvatar avatar = ProfileAvatar.of(color, null, character);
                assertEquals(character, avatar.character());
                assertEquals(color, avatar.preset());
            }
        }
        assertEquals("my-melody", ProfileAvatar.defaultAvatar().character());
        assertThrows(InvalidProfileAvatarException.class, () -> ProfileAvatar.of("peach", null, "unknown"));
    }

    @Test
    void defaultAvatarUsesPeachPreset() {
        ProfileAvatar avatar = ProfileAvatar.defaultAvatar();

        assertEquals("peach", avatar.preset());
        assertNull(avatar.imageDataUrl());
    }

    @Test
    void ofAcceptsKnownPresetAndCustomImage() {
        String image = "data:image/jpeg;base64,abc";

        ProfileAvatar avatar = ProfileAvatar.of("sky", image);

        assertEquals("sky", avatar.preset());
        assertEquals(image, avatar.imageDataUrl());
        assertTrue(avatar.hasCustomImage());
    }

    @Test
    void ofFallsBackToDefaultPresetWhenBlank() {
        ProfileAvatar avatar = ProfileAvatar.of("  ", null);

        assertEquals("peach", avatar.preset());
    }

    @Test
    void ofRejectsUnknownPreset() {
        assertThrows(InvalidProfileAvatarException.class, () -> ProfileAvatar.of("dragon", null));
    }

    @Test
    void ofRejectsNonImageDataUrl() {
        assertThrows(InvalidProfileAvatarException.class, () -> ProfileAvatar.of("peach", "https://example.com/a.png"));
    }

    @Test
    void ofRejectsOversizedImage() {
        String oversized = "data:image/jpeg;base64," + "a".repeat(ProfileAvatar.MAX_IMAGE_CHARS);

        assertThrows(InvalidProfileAvatarException.class, () -> ProfileAvatar.of("peach", oversized));
    }
}
