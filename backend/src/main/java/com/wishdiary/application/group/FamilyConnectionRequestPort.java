package com.wishdiary.application.group;
import com.wishdiary.domain.group.FamilyConnectionRequest;
import java.util.List;
import java.util.Optional;
public interface FamilyConnectionRequestPort {
    Optional<FamilyConnectionRequest> findById(Long id);
    Optional<FamilyConnectionRequest> findByPair(Long familyId, Long childId);
    List<FamilyConnectionRequest> findByFamilyId(Long familyId);
    List<FamilyConnectionRequest> findByChildId(Long childId);
    FamilyConnectionRequest save(FamilyConnectionRequest request);
}
