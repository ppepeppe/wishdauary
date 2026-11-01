package com.wishdiary.application.auth;

public record LoginResponse(
        Long userId,
        Long groupId,
        String appRole,
        String memberRole,
        String nickname,
        String relationLabel,
        String inviteCode,
        String accessToken
) {
}
