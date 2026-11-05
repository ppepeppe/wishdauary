package com.wishdiary.adapter.out.persistence.auth;

import com.wishdiary.adapter.out.persistence.user.GroupMemberJpaEntity;
import com.wishdiary.adapter.out.persistence.user.GroupMemberJpaRepository;
import com.wishdiary.adapter.out.persistence.user.UserJpaEntity;
import com.wishdiary.adapter.out.persistence.user.UserJpaRepository;
import com.wishdiary.application.auth.LoginPersistencePort;
import com.wishdiary.application.auth.LoadedLoginUser;
import com.wishdiary.domain.auth.LoginUserNotFoundException;
import com.wishdiary.domain.auth.UserGroupMembershipNotFoundException;
import com.wishdiary.domain.group.GroupNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class LoginPersistenceAdapter implements LoginPersistencePort {

    private final UserJpaRepository userJpaRepository;
    private final GroupMemberJpaRepository groupMemberJpaRepository;
    private final GroupJpaRepository groupJpaRepository;

    public LoginPersistenceAdapter(
            UserJpaRepository userJpaRepository,
            GroupMemberJpaRepository groupMemberJpaRepository,
            GroupJpaRepository groupJpaRepository
    ) {
        this.userJpaRepository = userJpaRepository;
        this.groupMemberJpaRepository = groupMemberJpaRepository;
        this.groupJpaRepository = groupJpaRepository;
    }

    @Override
    public LoadedLoginUser loadByEmail(String email) {
        UserJpaEntity user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new LoginUserNotFoundException(email));
        GroupMemberJpaEntity groupMember = groupMemberJpaRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserGroupMembershipNotFoundException(user.getId()));
        GroupJpaEntity group = groupJpaRepository.findById(groupMember.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException(groupMember.getGroupId()));
        return new LoadedLoginUser(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAppRole(),
                user.getPasswordHash(),
                groupMember.getGroupId(),
                groupMember.getMemberRole().name(),
                groupMember.getRelationLabel(),
                group.getInviteCode()
        );
    }
}
