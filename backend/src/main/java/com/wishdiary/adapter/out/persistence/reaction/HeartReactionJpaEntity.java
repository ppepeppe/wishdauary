package com.wishdiary.adapter.out.persistence.reaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "heart_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_heart_reaction_diary_family_user",
                columnNames = {"diaryId", "familyUserId"}
        )
)
public class HeartReactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long diaryId;

    @Column(nullable = false)
    private Long familyUserId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected HeartReactionJpaEntity() {
    }

    public HeartReactionJpaEntity(Long id, Long diaryId, Long familyUserId, LocalDateTime createdAt) {
        this.id = id;
        this.diaryId = diaryId;
        this.familyUserId = familyUserId;
        this.createdAt = createdAt;
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
