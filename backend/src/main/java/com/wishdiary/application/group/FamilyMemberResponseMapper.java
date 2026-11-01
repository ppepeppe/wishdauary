package com.wishdiary.application.group;

import com.wishdiary.application.user.LoadedUser;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FamilyMemberResponseMapper {

    public List<FamilyMemberResponse> toResponses(List<LoadedUser> familyMembers) {
        return familyMembers.stream()
                .map(this::toResponse)
                .toList();
    }

    public FamilyMemberResponse toResponse(LoadedUser familyMember) {
        return new FamilyMemberResponse(
                familyMember.groupMemberId(),
                familyMember.id(),
                familyMember.nickname(),
                familyMember.familyRelation() == null ? null : familyMember.familyRelation().name().toLowerCase(),
                familyMember.relationLabel()
        );
    }
}
