package com.wishdiary.application.group;
import java.time.LocalDateTime;
public record FamilyConnectionRequestResponse(Long requestId, Long familyUserId, String familyNickname,
        String relationLabel, Long childUserId, String childNickname, String status,
        LocalDateTime createdAt, LocalDateTime processedAt) { }
