package com.wishdiary.domain.group;

import java.time.LocalDateTime;

public record FamilyConnectionRequest(Long id, Long familyUserId, Long childUserId,
        Status status, LocalDateTime createdAt, LocalDateTime processedAt) {
    public enum Status { PENDING, APPROVED, REJECTED }

    public static FamilyConnectionRequest create(Long familyId, Long childId) {
        return new FamilyConnectionRequest(null, familyId, childId, Status.PENDING, LocalDateTime.now(), null);
    }

    public FamilyConnectionRequest renew() {
        return new FamilyConnectionRequest(id, familyUserId, childUserId, Status.PENDING, LocalDateTime.now(), null);
    }

    public FamilyConnectionRequest process(boolean approved) {
        if (status != Status.PENDING) {
            throw new FamilyConnectionRequestConflictException("이미 처리한 가족 연결 요청이에요.");
        }
        return new FamilyConnectionRequest(id, familyUserId, childUserId,
                approved ? Status.APPROVED : Status.REJECTED, createdAt, LocalDateTime.now());
    }
}
