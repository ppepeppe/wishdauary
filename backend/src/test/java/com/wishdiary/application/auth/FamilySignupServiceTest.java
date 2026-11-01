package com.wishdiary.application.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.wishdiary.domain.group.*;
import com.wishdiary.domain.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FamilySignupServiceTest {
    @Mock SignupPersistencePort persistence;
    @Mock PasswordEncoderPort passwords;
    @Mock AuthTokenIssuerPort tokens;
    private FamilySignupService service;

    @BeforeEach
    void setUp() {
        service = new FamilySignupService(persistence, passwords, tokens, new SignupPolicy(),
                new SignupInviteCodeGenerator(), new SignupResponseMapper());
    }

    @Test
    void signupCreatesAnIndependentFamilyAccount() {
        when(persistence.saveGroup(any())).thenReturn(new Group(10L, "INTERNAL", true));
        when(passwords.encode("password")).thenReturn("encoded");
        when(persistence.saveUser(any(), any())).thenReturn(new User(2L, "mother@test.com", "지연", AppRole.FAMILY));
        when(persistence.saveGroupMember(any())).thenReturn(new GroupMember(200L, 10L, 2L, GroupMemberRole.FAMILY, FamilyRelation.MOTHER, "엄마"));
        when(tokens.issue(any())).thenReturn("access-token");
        SignupResponse response = service.signup(new FamilySignupRequest("mother@test.com", "password", "지연", FamilyRelation.MOTHER, "엄마", "old-code"));
        assertEquals(2L, response.userId());
        assertEquals("access-token", response.accessToken());
        assertNull(response.inviteCode());
        verify(persistence, never()).loadGroupByInviteCode(anyString());
        ArgumentCaptor<Group> group = ArgumentCaptor.forClass(Group.class);
        verify(persistence).saveGroup(group.capture());
        assertTrue(group.getValue().inviteCodeRevoked());
    }
}
