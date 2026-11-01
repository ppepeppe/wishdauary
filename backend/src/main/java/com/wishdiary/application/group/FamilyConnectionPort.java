package com.wishdiary.application.group;

import java.util.List;

/** Family relationships come from active sharing groups owned by a child. */
public interface FamilyConnectionPort {
    List<Long> findFamilyUserIds(Long childUserId);
    List<Long> findChildUserIds(Long familyUserId);
    boolean isConnected(Long childUserId, Long familyUserId);
}
