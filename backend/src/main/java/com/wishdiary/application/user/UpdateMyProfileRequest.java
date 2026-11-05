package com.wishdiary.application.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateMyProfileRequest(
        @NotBlank String nickname,
        String avatarPreset,
        String avatarImage,
        String avatarCharacter
) {
    public UpdateMyProfileRequest(String nickname, String avatarPreset, String avatarImage) {
        this(nickname, avatarPreset, avatarImage, null);
    }
}
