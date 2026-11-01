package com.wishdiary.application.group;

public record FamilyMemberResponse(
        Long groupMemberId,
        Long userId,
        String nickname,
        String familyRelation,
        String relationLabel
) {
}
