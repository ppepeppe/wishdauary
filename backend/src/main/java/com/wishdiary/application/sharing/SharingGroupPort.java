package com.wishdiary.application.sharing;

import com.wishdiary.domain.sharing.SharingGroup;
import java.util.List;
import java.util.Optional;

public interface SharingGroupPort {
    void lockOwner(Long ownerId);
    List<SharingGroup> findActiveByOwnerId(Long ownerId);
    Optional<SharingGroup> findById(Long groupId);
    SharingGroup save(SharingGroup group);
    boolean isMemberOfAny(Long ownerId, List<Long> groupIds, Long viewerId);
}
