package com.wishdiary.application.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MyProfileResponse(
        Long userId,
        String loginId,
        String nickname,
        String appRole,
        String avatarPreset,
        String avatarImage,
        String avatarCharacter,
        String userRole
) {
    // Older app builds still read this response property.
    @JsonProperty("email")
    public String legacyEmail() { return loginId; }
}
