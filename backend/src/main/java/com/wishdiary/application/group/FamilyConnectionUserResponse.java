package com.wishdiary.application.group;
public record FamilyConnectionUserResponse(Long userId, String nickname, String relationLabel,
        String avatarPreset, String avatarCharacter, String avatarImage) { }
