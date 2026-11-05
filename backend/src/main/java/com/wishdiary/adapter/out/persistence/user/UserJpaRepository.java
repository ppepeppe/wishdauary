package com.wishdiary.adapter.out.persistence.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    List<UserJpaEntity> findTop20ByAppRoleAndNicknameContainingIgnoreCaseOrderByNicknameAscIdAsc(
            com.wishdiary.domain.user.AppRole role, String nickname);

    List<UserJpaEntity> findTop20ByNicknameContainingIgnoreCaseAndIdNotOrderByNicknameAscIdAsc(String nickname, Long excludedUserId);

    List<UserJpaEntity> findAllByIdIn(List<Long> userIds);

    boolean existsByEmail(String email);

    java.util.Optional<UserJpaEntity> findByEmail(String email);
}
