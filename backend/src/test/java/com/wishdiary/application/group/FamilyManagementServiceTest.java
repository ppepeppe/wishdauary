package com.wishdiary.application.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.FamilyManagementAccessDeniedException;
import com.wishdiary.domain.group.FamilyRelation;
import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FamilyManagementServiceTest {

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private FamilyGroupManagementPort familyGroupManagementPort;

    @Mock
    private FamilyInviteCodeGenerator familyInviteCodeGenerator;

    private FamilyManagementService familyManagementService;

    @BeforeEach
    void setUp() {
        familyManagementService = new FamilyManagementService(
                loadUserPort,
                familyGroupManagementPort,
                familyInviteCodeGenerator,
                new FamilyMemberResponseMapper()
        );
    }

    @Test
    void getMembersReturnsConnectedFamilyMembers() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadUserPort.loadFamilyMembersByGroupId(100L)).thenReturn(List.of(motherUser()));

        List<FamilyMemberResponse> responses = familyManagementService.getMembers(1L);

        assertEquals(1, responses.size());
        assertEquals(21L, responses.get(0).groupMemberId());
        assertEquals("엄마", responses.get(0).relationLabel());
    }

    @Test
    void regenerateInviteCodeCreatesActiveNewCode() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(familyGroupManagementPort.loadGroupById(100L)).thenReturn(new Group(100L, "OLD12345", true));
        when(familyInviteCodeGenerator.generate()).thenReturn("NEW12345");
        when(familyGroupManagementPort.existsByInviteCode("NEW12345")).thenReturn(false);
        when(familyGroupManagementPort.saveGroup(new Group(100L, "NEW12345", false)))
                .thenReturn(new Group(100L, "NEW12345", false));

        FamilyInviteCodeResponse response = familyManagementService.regenerateInviteCode(1L);

        assertEquals("NEW12345", response.inviteCode());
        assertTrue(response.active());
    }

    @Test
    void revokeInviteCodeMakesCurrentCodeInactive() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(familyGroupManagementPort.loadGroupById(100L)).thenReturn(new Group(100L, "CODE1234", false));
        when(familyGroupManagementPort.saveGroup(new Group(100L, "CODE1234", true)))
                .thenReturn(new Group(100L, "CODE1234", true));

        FamilyInviteCodeResponse response = familyManagementService.revokeInviteCode(1L);

        assertFalse(response.active());
    }

    @Test
    void familyUserCannotManageFamily() {
        when(loadUserPort.loadById(2L)).thenReturn(motherUser());

        assertThrows(FamilyManagementAccessDeniedException.class, () -> familyManagementService.getMembers(2L));
    }

    @Test
    void removeMemberDeletesFamilyMemberInSameGroup() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(familyGroupManagementPort.loadFamilyMemberByGroupMemberId(21L)).thenReturn(Optional.of(motherUser()));

        familyManagementService.removeMember(1L, 21L);

        verify(familyGroupManagementPort).deleteFamilyMemberByGroupMemberId(21L);
    }

    private LoadedUser childUser() {
        return new LoadedUser(1L, "child@test.com", "미아", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null);
    }

    private LoadedUser motherUser() {
        return new LoadedUser(2L, "mother@test.com", "엄마", AppRole.FAMILY, 21L, 100L, GroupMemberRole.FAMILY, FamilyRelation.MOTHER, "엄마");
    }
}
