package com.wishdiary.adapter.out.persistence.group;

import com.wishdiary.adapter.out.persistence.auth.GroupJpaEntity;
import com.wishdiary.adapter.out.persistence.auth.GroupJpaRepository;
import com.wishdiary.adapter.out.persistence.user.GroupMemberJpaEntity;
import com.wishdiary.adapter.out.persistence.user.GroupMemberJpaRepository;
import com.wishdiary.adapter.out.persistence.user.UserJpaEntity;
import com.wishdiary.adapter.out.persistence.user.UserJpaRepository;
import com.wishdiary.application.group.FamilyGroupManagementPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.group.GroupNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FamilyManagementPersistenceAdapter implements FamilyGroupManagementPort {

    private final GroupJpaRepository groupJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public FamilyManagementPersistenceAdapter(
            GroupJpaRepository groupJpaRepository,
            GroupMemberJpaRepository groupMemberJpaRepository,
            UserJpaRepository userJpaRepository
    ) {
        this.groupJpaRepository = groupJpaRepository;
        this.groupMemberJpaRepository = groupMemberJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Group loadGroupById(Long groupId) {
        return groupJpaRepository.findById(groupId)
                .map(this::toGroup)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    @Override
    public Group saveGroup(Group group) {
        GroupJpaEntity savedEntity = groupJpaRepository.save(new GroupJpaEntity(
                group.id(),
                group.inviteCode(),
                group.inviteCodeRevoked()
        ));
        return toGroup(savedEntity);
    }

    @Override
    public boolean existsByInviteCode(String inviteCode) {
        return groupJpaRepository.existsByInviteCode(inviteCode);
    }

    @Override
    public Optional<LoadedUser> loadFamilyMemberByGroupMemberId(Long groupMemberId) {
        return groupMemberJpaRepository.findById(groupMemberId)
                .filter(groupMember -> groupMember.getMemberRole() == GroupMemberRole.FAMILY)
                .flatMap(groupMember -> userJpaRepository.findById(groupMember.getUserId())
                        .map(user -> toLoadedUser(user, groupMember)));
    }

    @Override
    public void deleteFamilyMemberByGroupMemberId(Long groupMemberId) {
        groupMemberJpaRepository.deleteById(groupMemberId);
    }

    private Group toGroup(GroupJpaEntity entity) {
        return new Group(entity.getId(), entity.getInviteCode(), entity.isInviteCodeRevoked());
    }

    private LoadedUser toLoadedUser(UserJpaEntity user, GroupMemberJpaEntity groupMember) {
        return new LoadedUser(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAppRole(),
                groupMember.getId(),
                groupMember.getGroupId(),
                groupMember.getMemberRole(),
                groupMember.getFamilyRelation(),
                groupMember.getRelationLabel()
        );
    }
}
