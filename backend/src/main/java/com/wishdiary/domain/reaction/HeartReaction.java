package com.wishdiary.domain.reaction;

import java.time.LocalDateTime;

public class HeartReaction {

    private final Long id;
    private final Long diaryId;
    private final Long familyUserId;
    private final LocalDateTime createdAt;

    private HeartReaction(Long id, Long diaryId, Long familyUserId, LocalDateTime createdAt) {
        this.id = id;
        this.diaryId = diaryId;
        this.familyUserId = familyUserId;
        this.createdAt = createdAt;
    }

    public static HeartReaction create(Long diaryId, Long familyUserId) {
        return new HeartReaction(null, diaryId, familyUserId, LocalDateTime.now());
    }

    public static HeartReaction reconstitute(Long id, Long diaryId, Long familyUserId, LocalDateTime createdAt) {
        return new HeartReaction(id, diaryId, familyUserId, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getDiaryId() {
        return diaryId;
    }

    public Long getFamilyUserId() {
        return familyUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
