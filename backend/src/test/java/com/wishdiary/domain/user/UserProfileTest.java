package com.wishdiary.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void profileChangesPreserveAdminRoleSeparatelyFromAppRole() {
        User admin = new User(1L, "admin@test.com", "관리자", AppRole.FAMILY,
                ProfileAvatar.defaultAvatar(), UserRole.ADMIN);
        User updated = admin.changeProfile("새 이름", ProfileAvatar.of("mint", null));
        assertEquals(UserRole.ADMIN, updated.userRole());
        assertEquals(AppRole.FAMILY, updated.appRole());
        assertEquals(UserRole.USER, new User(2L, "user@test.com", "아이", AppRole.CHILD).userRole());
    }

    @Test
    void changeProfileUpdatesNicknameAndAvatar() {
        User user = new User(1L, "child@test.com", "미아", AppRole.CHILD);

        User updated = user.changeProfile("별이", ProfileAvatar.of("mint", null));

        assertEquals("별이", updated.nickname());
        assertEquals("mint", updated.avatar().preset());
        assertNull(updated.avatar().imageDataUrl());
        assertEquals(AppRole.CHILD, updated.appRole());
        assertEquals("child@test.com", updated.email());
    }

    @Test
    void changeProfileTrimsNickname() {
        User user = new User(1L, "child@test.com", "미아", AppRole.CHILD);

        User updated = user.changeProfile("  구름  ", ProfileAvatar.defaultAvatar());

        assertEquals("구름", updated.nickname());
    }

    @Test
    void changeProfileRejectsBlankNickname() {
        User user = new User(1L, "child@test.com", "미아", AppRole.CHILD);

        assertThrows(InvalidUserNicknameException.class, () -> user.changeProfile("   ", ProfileAvatar.defaultAvatar()));
    }

    @Test
    void changeProfileRejectsTooLongNickname() {
        User user = new User(1L, "child@test.com", "미아", AppRole.CHILD);

        assertThrows(
                InvalidUserNicknameException.class,
                () -> user.changeProfile("가".repeat(21), ProfileAvatar.defaultAvatar())
        );
    }

    @Test
    void changeProfileKeepsDefaultAvatarWhenAvatarIsNull() {
        User user = new User(1L, "child@test.com", "미아", AppRole.CHILD);

        User updated = user.changeProfile("미아", null);

        assertEquals("peach", updated.avatar().preset());
    }
}
