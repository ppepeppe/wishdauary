package com.wishdiary.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.user.InvalidUserNicknameException;
import com.wishdiary.domain.user.ProfileAvatar;
import com.wishdiary.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyProfileServiceTest {

    @Mock
    private LoadUserAccountPort loadUserAccountPort;

    @Mock
    private SaveUserProfilePort saveUserProfilePort;

    private MyProfileService myProfileService;

    @BeforeEach
    void setUp() {
        myProfileService = new MyProfileService(loadUserAccountPort, saveUserProfilePort);
    }

    @Test
    void getReturnsCurrentProfile() {
        when(loadUserAccountPort.loadAccount(1L))
                .thenReturn(new User(1L, "child@test.com", "미아", AppRole.CHILD, ProfileAvatar.of("mint", null)));

        MyProfileResponse response = myProfileService.get(1L);

        assertEquals(1L, response.userId());
        assertEquals("미아", response.nickname());
        assertEquals("child", response.appRole());
        assertEquals("mint", response.avatarPreset());
        assertNull(response.avatarImage());
    }

    @Test
    void updateSavesChangedNicknameAndAvatar() {
        when(loadUserAccountPort.loadAccount(1L))
                .thenReturn(new User(1L, "child@test.com", "미아", AppRole.CHILD));
        when(saveUserProfilePort.saveProfile(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MyProfileResponse response = myProfileService.update(
                1L,
                new UpdateMyProfileRequest("별이", "sky", null)
        );

        assertEquals("별이", response.nickname());
        assertEquals("sky", response.avatarPreset());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserProfilePort).saveProfile(captor.capture());
        assertEquals("별이", captor.getValue().nickname());
        assertEquals("sky", captor.getValue().avatar().preset());
    }

    @Test
    void updateRejectsBlankNickname() {
        when(loadUserAccountPort.loadAccount(1L))
                .thenReturn(new User(1L, "child@test.com", "미아", AppRole.CHILD));

        assertThrows(
                InvalidUserNicknameException.class,
                () -> myProfileService.update(1L, new UpdateMyProfileRequest("  ", "peach", null))
        );
    }
}
