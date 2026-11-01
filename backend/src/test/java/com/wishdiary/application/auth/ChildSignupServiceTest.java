package com.wishdiary.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.GroupMember;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.user.DuplicateUserEmailException;
import com.wishdiary.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChildSignupServiceTest {

    @Mock
    private SignupPersistencePort signupPersistencePort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private AuthTokenIssuerPort authTokenIssuerPort;

    private ChildSignupService childSignupService;

    @BeforeEach
    void setUp() {
        childSignupService = new ChildSignupService(
                signupPersistencePort,
                passwordEncoderPort,
                authTokenIssuerPort,
                new SignupPolicy(),
                new SignupInviteCodeGenerator(),
                new SignupResponseMapper()
        );
    }

    @Test
    void signupCreatesChildUserGroupAndMembership() {
        when(signupPersistencePort.existsUserByEmail("child@test.com")).thenReturn(false);
        when(passwordEncoderPort.encode("password")).thenReturn("encoded");
        when(signupPersistencePort.saveUser(any(User.class), any(String.class)))
                .thenReturn(new User(1L, "child@test.com", "미아", AppRole.CHILD));
        when(signupPersistencePort.saveGroup(any(Group.class)))
                .thenAnswer(invocation -> {
                    Group group = invocation.getArgument(0);
                    return new Group(10L, group.inviteCode());
                });
        when(signupPersistencePort.saveGroupMember(any(GroupMember.class)))
                .thenReturn(new GroupMember(100L, 10L, 1L, GroupMemberRole.CHILD, null, null));
        when(authTokenIssuerPort.issue(any(AuthTokenIssueRequest.class))).thenReturn("access-token");

        SignupResponse response = childSignupService.signup(
                new ChildSignupRequest("child@test.com", "password", "미아")
        );

        assertEquals(1L, response.userId());
        assertEquals(10L, response.groupId());
        assertEquals("child", response.appRole());
        assertEquals("access-token", response.accessToken());

        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(signupPersistencePort).saveGroupMember(memberCaptor.capture());
        assertEquals(GroupMemberRole.CHILD, memberCaptor.getValue().memberRole());
    }

    @Test
    void signupFailsWhenEmailAlreadyExists() {
        when(signupPersistencePort.existsUserByEmail("child@test.com")).thenReturn(true);

        assertThrows(
                DuplicateUserEmailException.class,
                () -> childSignupService.signup(new ChildSignupRequest("child@test.com", "password", "미아"))
        );

        verify(signupPersistencePort, never()).saveUser(any(User.class), any(String.class));
    }
}
