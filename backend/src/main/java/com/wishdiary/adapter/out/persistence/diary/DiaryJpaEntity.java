package com.wishdiary.adapter.out.persistence.diary;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diaries")
public class DiaryJpaEntity {

    @ElementCollection
    @CollectionTable(name = "diary_audience_groups", joinColumns = @JoinColumn(name = "diary_id"))
    @Column(name = "sharing_group_id", nullable = false)
    private Set<Long> sharingGroupIds = new HashSet<>();

    public List<Long> getSharingGroupIds() { return sharingGroupIds.stream().sorted().toList(); }
    public void setSharingGroupIds(List<Long> ids) { sharingGroupIds = new HashSet<>(ids); }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(length = 20)
    private String emotion;

    @Column(nullable = false, length = 20)
    private String visibilityScope;

    @Column(nullable = false)
    private LocalDate writtenDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected DiaryJpaEntity() {
    }

    public DiaryJpaEntity(
            Long id,
            Long authorId,
            String content,
            String emotion,
            String visibilityScope,
            LocalDate writtenDate,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.emotion = emotion;
        this.visibilityScope = visibilityScope;
        this.writtenDate = writtenDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public String getEmotion() {
        return emotion;
    }

    public String getVisibilityScope() {
        return visibilityScope;
    }

    public LocalDate getWrittenDate() {
        return writtenDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
