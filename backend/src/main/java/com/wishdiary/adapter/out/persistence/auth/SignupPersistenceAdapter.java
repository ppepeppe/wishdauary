package com.wishdiary.adapter.out.persistence.auth;

import com.wishdiary.adapter.out.persistence.user.GroupMemberJpaEntity;
import com.wishdiary.adapter.out.persistence.user.GroupMemberJpaRepository;
import com.wishdiary.adapter.out.persistence.user.UserJpaEntity;
import com.wishdiary.adapter.out.persistence.user.UserJpaRepository;
import com.wishdiary.application.auth.SignupPersistencePort;
import com.wishdiary.domain.group.Group;
import com.wishdiary.domain.group.GroupMember;
import com.wishdiary.domain.group.GroupNotFoundByInviteCodeException;
import com.wishdiary.domain.group.GroupNotFoundException;
import com.wishdiary.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class SignupPersistenceAdapter implements SignupPersistencePort {

    private final UserJpaRepository userJpaRepository;
    private final GroupJpaRepository groupJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;

    public SignupPersistenceAdapter(
            UserJpaRepository userJpaRepository,
            GroupJpaRepository groupJpaRepository,
            GroupMemberJpaRepository groupMemberJpaRepository
    ) {
        this.userJpaRepository = userJpaRepository;
        this.groupJpaRepository = groupJpaRepository;
        this.groupMemberJpaRepository = groupMemberJpaRepository;
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User saveUser(User user, String encodedPassword) {
        UserJpaEntity savedEntity = userJpaRepository.save(new UserJpaEntity(
                user.id(),
                user.email(),
                user.nickname(),
                user.appRole(),
                encodedPassword
        ));
        return new User(savedEntity.getId(), savedEntity.getEmail(), savedEntity.getNickname(), savedEntity.getAppRole());
    }

    @Override
    public Group saveGroup(Group group) {
        GroupJpaEntity savedEntity = groupJpaRepository.save(new GroupJpaEntity(
                group.id(),
                group.inviteCode(),
                group.inviteCodeRevoked()
        ));
        return new Group(savedEntity.getId(), savedEntity.getInviteCode(), savedEntity.isInviteCodeRevoked());
    }

    @Override
    public Group loadGroupById(Long groupId) {
        GroupJpaEntity groupEntity = groupJpaRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        return new Group(groupEntity.getId(), groupEntity.getInviteCode(), groupEntity.isInviteCodeRevoked());
    }

    @Override
    public Group loadGroupByInviteCode(String inviteCode) {
        GroupJpaEntity groupEntity = groupJpaRepository.findByInviteCodeAndInviteCodeRevokedFalse(inviteCode)
                .orElseThrow(() -> new GroupNotFoundByInviteCodeException(inviteCode));
        return new Group(groupEntity.getId(), groupEntity.getInviteCode(), groupEntity.isInviteCodeRevoked());
    }

    @Override
    public GroupMember saveGroupMember(GroupMember groupMember) {
        GroupMemberJpaEntity savedEntity = groupMemberJpaRepository.save(new GroupMemberJpaEntity(
                groupMember.id(),
                groupMember.groupId(),
                groupMember.userId(),
                groupMember.memberRole(),
                groupMember.familyRelation(),
                groupMember.relationLabel()
        ));
        return new GroupMember(
                savedEntity.getId(),
                savedEntity.getGroupId(),
                savedEntity.getUserId(),
                savedEntity.getMemberRole(),
                savedEntity.getFamilyRelation(),
                savedEntity.getRelationLabel()
        );
    }
}
