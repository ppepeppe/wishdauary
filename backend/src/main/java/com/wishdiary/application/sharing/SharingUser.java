package com.wishdiary.application.sharing;

public record SharingUser(Long userId, String nickname, String appRole,
        String avatarPreset, String avatarCharacter, String avatarImage) {
    public SharingUser(Long userId, String nickname, String appRole) {
        this(userId, nickname, appRole, null, null, null);
    }
}
