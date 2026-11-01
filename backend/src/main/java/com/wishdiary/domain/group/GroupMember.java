package com.wishdiary.domain.group;

public record GroupMember(
        Long id,
        Long groupId,
        Long userId,
        GroupMemberRole memberRole,
        FamilyRelation familyRelation,
        String relationLabel
) {
}
