package com.wishdiary.application.user;

import com.wishdiary.domain.group.FamilyRelation;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.user.AppRole;

public record LoadedUser(
        Long id,
        String email,
        String nickname,
        AppRole appRole,
        Long groupMemberId,
        Long groupId,
        GroupMemberRole memberRole,
        FamilyRelation familyRelation,
        String relationLabel
) {

    public boolean isChild() {
        return appRole.isChild() && memberRole != null && memberRole.isChild();
    }

    public boolean isFamily() {
        return appRole.isFamily() && memberRole != null && memberRole.isFamily();
    }
}
