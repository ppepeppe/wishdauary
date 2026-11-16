package com.wishdiary.application.user;

import com.wishdiary.domain.user.FamilyRelationship;

public interface SaveFamilyRelationshipPort {
    void saveRelationship(Long userId, FamilyRelationship relationship);
}
