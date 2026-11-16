package com.wishdiary.adapter.out.persistence.user;

import com.wishdiary.application.user.LoadFamilyRelationshipPort;
import com.wishdiary.application.user.SaveFamilyRelationshipPort;
import com.wishdiary.domain.auth.UserGroupMembershipNotFoundException;
import com.wishdiary.domain.user.FamilyRelationship;
import org.springframework.stereotype.Component;

@Component
public class FamilyRelationshipPersistenceAdapter implements LoadFamilyRelationshipPort, SaveFamilyRelationshipPort {
    private final GroupMemberJpaRepository members;

    public FamilyRelationshipPersistenceAdapter(GroupMemberJpaRepository members) { this.members = members; }

    @Override
    public String loadRelationLabel(Long userId) { return requireMember(userId).getRelationLabel(); }

    @Override
    public void saveRelationship(Long userId, FamilyRelationship relationship) {
        GroupMemberJpaEntity member = requireMember(userId);
        member.updateRelationship(relationship);
        members.save(member);
    }

    private GroupMemberJpaEntity requireMember(Long userId) {
        return members.findByUserId(userId).orElseThrow(() -> new UserGroupMembershipNotFoundException(userId));
    }
}
