package com.wishdiary.application.auth;

public record SignupResponse(
        Long userId,
        Long groupId,
        Long groupMemberId,
        String appRole,
        String memberRole,
        String nickname,
        String familyRelation,
        String relationLabel,
        String inviteCode,
        String accessToken
) {
}
