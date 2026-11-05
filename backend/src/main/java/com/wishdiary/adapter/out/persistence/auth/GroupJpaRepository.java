package com.wishdiary.adapter.out.persistence.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, Long> {

    Optional<GroupJpaEntity> findByInviteCode(String inviteCode);

    Optional<GroupJpaEntity> findByInviteCodeAndInviteCodeRevokedFalse(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
