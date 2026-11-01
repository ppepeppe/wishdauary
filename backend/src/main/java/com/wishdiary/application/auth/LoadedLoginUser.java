package com.wishdiary.application.auth;

import com.wishdiary.domain.user.AppRole;

public record LoadedLoginUser(
        Long userId,
        String email,
        String nickname,
        AppRole appRole,
        String passwordHash,
        Long groupId,
        String memberRole,
        String relationLabel,
        String inviteCode
) {
}
