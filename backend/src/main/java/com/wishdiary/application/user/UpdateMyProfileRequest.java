package com.wishdiary.application.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
        @NotBlank String nickname,
        String avatarPreset,
        String avatarImage,
        String avatarCharacter,
        @Size(max = 100) String relationLabel
) {
    public UpdateMyProfileRequest(String nickname, String avatarPreset, String avatarImage, String avatarCharacter) {
        this(nickname, avatarPreset, avatarImage, avatarCharacter, null);
    }

    public UpdateMyProfileRequest(String nickname, String avatarPreset, String avatarImage) {
        this(nickname, avatarPreset, avatarImage, null, null);
    }
}
