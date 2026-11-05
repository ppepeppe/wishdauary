package com.wishdiary.adapter.out.persistence.sharing;

import com.wishdiary.adapter.out.persistence.user.UserJpaEntity;
import com.wishdiary.adapter.out.persistence.user.UserJpaRepository;
import com.wishdiary.application.sharing.SharingUser;
import com.wishdiary.application.sharing.SharingUserDirectoryPort;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SharingUserDirectoryAdapter implements SharingUserDirectoryPort {
    private final UserJpaRepository users;

    public SharingUserDirectoryAdapter(UserJpaRepository users) { this.users = users; }

    public List<SharingUser> search(String nickname, Long excludedUserId) {
        return users.findTop20ByNicknameContainingIgnoreCaseAndIdNotOrderByNicknameAscIdAsc(nickname, excludedUserId)
                .stream().map(this::toResponse).toList();
    }

    public List<SharingUser> findByIds(Set<Long> userIds) {
        return users.findAllByIdIn(List.copyOf(userIds)).stream().map(this::toResponse).toList();
    }

    public List<SharingUser> searchChildren(String nickname) {
        return users.findTop20ByAppRoleAndNicknameContainingIgnoreCaseOrderByNicknameAscIdAsc(
                com.wishdiary.domain.user.AppRole.CHILD, nickname).stream().map(this::toResponse).toList();
    }

    private SharingUser toResponse(UserJpaEntity user) {
        return new SharingUser(user.getId(), user.getNickname(), user.getAppRole().name().toLowerCase());
    }
}
