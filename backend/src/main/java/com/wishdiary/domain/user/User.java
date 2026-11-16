package com.wishdiary.domain.user;

public record User(
        Long id,
        String loginId,
        String nickname,
        AppRole appRole,
        ProfileAvatar avatar,
        UserRole userRole
) {

    public User {
        userRole = userRole == null ? UserRole.USER : userRole;
    }

    public User(Long id, String loginId, String nickname, AppRole appRole, ProfileAvatar avatar) {
        this(id, loginId, nickname, appRole, avatar, UserRole.USER);
    }

    public User(Long id, String loginId, String nickname, AppRole appRole) {
        this(id, loginId, nickname, appRole, ProfileAvatar.defaultAvatar());
    }

    public User changeProfile(String nickname, ProfileAvatar avatar) {
        return new User(
                id,
                loginId,
                requireNickname(nickname),
                appRole,
                avatar == null ? ProfileAvatar.defaultAvatar() : avatar,
                userRole
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
