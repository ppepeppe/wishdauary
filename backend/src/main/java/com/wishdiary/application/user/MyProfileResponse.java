package com.wishdiary.application.user;

public record MyProfileResponse(
        Long userId,
        String email,
        String nickname,
        String appRole,
        String avatarPreset,
        String avatarImage
) {
}
