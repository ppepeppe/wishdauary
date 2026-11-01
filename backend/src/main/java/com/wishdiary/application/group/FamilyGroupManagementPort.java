package com.wishdiary.application.group;

import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.Group;
import java.util.Optional;

public interface FamilyGroupManagementPort {

    Group loadGroupById(Long groupId);

    Group saveGroup(Group group);

    boolean existsByInviteCode(String inviteCode);

    Optional<LoadedUser> loadFamilyMemberByGroupMemberId(Long groupMemberId);

    void deleteFamilyMemberByGroupMemberId(Long groupMemberId);
}
