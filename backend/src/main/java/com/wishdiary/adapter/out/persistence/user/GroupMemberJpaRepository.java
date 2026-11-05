package com.wishdiary.adapter.out.persistence.user;

import com.wishdiary.domain.group.GroupMemberRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberJpaEntity, Long> {

    Optional<GroupMemberJpaEntity> findByUserId(Long userId);

    List<GroupMemberJpaEntity> findAllByGroupIdAndMemberRoleOrderByUserIdAsc(Long groupId, GroupMemberRole memberRole);

    List<GroupMemberJpaEntity> findAllByUserIdIn(List<Long> userIds);
}
