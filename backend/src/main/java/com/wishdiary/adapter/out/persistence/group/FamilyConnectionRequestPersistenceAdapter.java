package com.wishdiary.adapter.out.persistence.group;
import com.wishdiary.application.group.FamilyConnectionRequestPort;
import com.wishdiary.domain.group.FamilyConnectionRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FamilyConnectionRequestPersistenceAdapter implements FamilyConnectionRequestPort {
    private final FamilyConnectionRequestJpaRepository requests;
    public FamilyConnectionRequestPersistenceAdapter(FamilyConnectionRequestJpaRepository requests) { this.requests = requests; }
    public Optional<FamilyConnectionRequest> findById(Long id) { return requests.findById(id).map(FamilyConnectionRequestJpaEntity::toDomain); }
    public Optional<FamilyConnectionRequest> findByPair(Long familyId, Long childId) {
        return requests.findByFamilyUserIdAndChildUserId(familyId, childId).map(FamilyConnectionRequestJpaEntity::toDomain);
    }
    public List<FamilyConnectionRequest> findByFamilyId(Long familyId) {
        return requests.findAllByFamilyUserIdOrderByCreatedAtDesc(familyId).stream().map(FamilyConnectionRequestJpaEntity::toDomain).toList();
    }
    public List<FamilyConnectionRequest> findByChildId(Long childId) {
        return requests.findAllByChildUserIdOrderByCreatedAtDesc(childId).stream().map(FamilyConnectionRequestJpaEntity::toDomain).toList();
    }
    public FamilyConnectionRequest save(FamilyConnectionRequest request) {
        return requests.saveAndFlush(new FamilyConnectionRequestJpaEntity(request)).toDomain();
    }
}
