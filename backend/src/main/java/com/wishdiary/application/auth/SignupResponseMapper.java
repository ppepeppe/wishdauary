package com.wishdiary.application.auth;

import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.GroupMember;
import com.wishdiary.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class SignupResponseMapper {

    public SignupResponse map(User user, Group group, GroupMember groupMember) {
        return new SignupResponse(
                user.id(),
                group.id(),
                groupMember.id(),
                user.appRole().name().toLowerCase(),
                groupMember.memberRole().name().toLowerCase(),
                user.nickname(),
                groupMember.familyRelation() == null ? null : groupMember.familyRelation().name().toLowerCase(),
                groupMember.relationLabel(),
                null,
                null
        );
    }
}
