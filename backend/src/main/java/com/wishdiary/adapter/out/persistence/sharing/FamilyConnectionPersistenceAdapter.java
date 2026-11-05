package com.wishdiary.adapter.out.persistence.sharing;

import com.wishdiary.application.group.FamilyConnectionPort;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FamilyConnectionPersistenceAdapter implements FamilyConnectionPort {
    private final SharingGroupJpaRepository groups;

    public FamilyConnectionPersistenceAdapter(SharingGroupJpaRepository groups) { this.groups = groups; }

    public List<Long> findFamilyUserIds(Long childUserId) { return groups.findFamilyUserIds(childUserId); }

    public List<Long> findChildUserIds(Long familyUserId) { return groups.findChildUserIds(familyUserId); }

    public boolean isConnected(Long childUserId, Long familyUserId) {
        return findFamilyUserIds(childUserId).contains(familyUserId);
    }
}
