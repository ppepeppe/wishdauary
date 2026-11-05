package com.wishdiary.adapter.out.persistence.sharing;

import com.wishdiary.adapter.out.persistence.user.UserJpaEntity;
import com.wishdiary.application.sharing.SharingGroupPort;
import com.wishdiary.domain.sharing.SharingGroup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SharingGroupPersistenceAdapter implements SharingGroupPort {
    private final SharingGroupJpaRepository repository;
    private final EntityManager entityManager;

    public SharingGroupPersistenceAdapter(SharingGroupJpaRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    public void lockOwner(Long ownerId) {
        entityManager.find(UserJpaEntity.class, ownerId, LockModeType.PESSIMISTIC_WRITE);
    }

    public List<SharingGroup> findActiveByOwnerId(Long ownerId) {
        return repository.findAllByOwnerIdAndSlotIsNotNullOrderBySlot(ownerId).stream()
                .map(SharingGroupJpaEntity::toDomain).toList();
    }

    public Optional<SharingGroup> findById(Long groupId) {
        return repository.findById(groupId).map(SharingGroupJpaEntity::toDomain);
    }

    public SharingGroup save(SharingGroup group) {
        return repository.saveAndFlush(new SharingGroupJpaEntity(group)).toDomain();
    }

    public boolean isMemberOfAny(Long ownerId, List<Long> groupIds, Long viewerId) {
        return !groupIds.isEmpty() && repository.countMembership(ownerId, groupIds, viewerId) > 0;
    }
}
