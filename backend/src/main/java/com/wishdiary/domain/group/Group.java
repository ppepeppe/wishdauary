package com.wishdiary.domain.group;

public record Group(
        Long id,
        String inviteCode,
        boolean inviteCodeRevoked
) {

    public Group(Long id, String inviteCode) {
        this(id, inviteCode, false);
    }

    public Group regenerateInviteCode(String inviteCode) {
        return new Group(id, inviteCode, false);
    }

    public Group revokeInviteCode() {
        return new Group(id, inviteCode, true);
    }
}
