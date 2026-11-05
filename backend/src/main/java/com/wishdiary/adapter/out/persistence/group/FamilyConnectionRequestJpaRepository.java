package com.wishdiary.adapter.out.persistence.group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FamilyConnectionRequestJpaRepository extends JpaRepository<FamilyConnectionRequestJpaEntity, Long> {
    Optional<FamilyConnectionRequestJpaEntity> findByFamilyUserIdAndChildUserId(Long familyId, Long childId);
    List<FamilyConnectionRequestJpaEntity> findAllByFamilyUserIdOrderByCreatedAtDesc(Long familyId);
    List<FamilyConnectionRequestJpaEntity> findAllByChildUserIdOrderByCreatedAtDesc(Long childId);
}
