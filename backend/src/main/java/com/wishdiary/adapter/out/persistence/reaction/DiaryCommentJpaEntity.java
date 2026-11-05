package com.wishdiary.adapter.out.persistence.reaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "diary_comments")
public class DiaryCommentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long diaryId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected DiaryCommentJpaEntity() {
    }

    public DiaryCommentJpaEntity(Long id, Long diaryId, Long authorId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.diaryId = diaryId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getDiaryId() {
        return diaryId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
