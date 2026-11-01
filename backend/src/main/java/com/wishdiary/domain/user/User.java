package com.wishdiary.domain.user;

public record User(
        Long id,
        String email,
        String nickname,
        AppRole appRole,
        ProfileAvatar avatar
) {

    public User(Long id, String email, String nickname, AppRole appRole) {
        this(id, email, nickname, appRole, ProfileAvatar.defaultAvatar());
    }

    public User changeProfile(String nickname, ProfileAvatar avatar) {
        return new User(
                id,
                email,
                requireNickname(nickname),
                appRole,
                avatar == null ? ProfileAvatar.defaultAvatar() : avatar
        );
    }

    private static String requireNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new InvalidUserNicknameException();
        }

        String trimmed = nickname.trim();
        if (trimmed.length() > 20) {
            throw new InvalidUserNicknameException();
        }
        return trimmed;
    }
}
