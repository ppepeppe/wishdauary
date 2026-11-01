package com.wishdiary.application.auth;

import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.GroupMember;
import com.wishdiary.domain.user.User;

public interface SignupPersistencePort {

    boolean existsUserByEmail(String email);

    User saveUser(User user, String encodedPassword);

    Group saveGroup(Group group);

    Group loadGroupById(Long groupId);

    Group loadGroupByInviteCode(String inviteCode);

    GroupMember saveGroupMember(GroupMember groupMember);
}
