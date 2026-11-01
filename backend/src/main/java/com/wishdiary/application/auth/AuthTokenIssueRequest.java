package com.wishdiary.application.auth;

public record AuthTokenIssueRequest(
        Long userId,
        Long groupId,
        String appRole,
        String nickname,
        String relationLabel,
        String inviteCode
) {
}
