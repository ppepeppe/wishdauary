package com.wishdiary.application.auth;

import com.wishdiary.domain.user.AppRole;

public record AuthenticatedSession(
        Long userId,
        Long groupId,
        AppRole appRole
) {

    public boolean isChild() {
        return appRole.isChild();
    }

    public boolean isFamily() {
        return appRole.isFamily();
    }
}
