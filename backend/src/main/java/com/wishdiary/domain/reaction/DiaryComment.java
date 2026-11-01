package com.wishdiary.domain.reaction;

import com.wishdiary.domain.diary.InvalidDiaryContentException;
import java.time.LocalDateTime;

public class DiaryComment {

    private final Long id;
    private final Long diaryId;
    private final Long authorId;
    private final String content;
    private final LocalDateTime createdAt;

    private DiaryComment(Long id, Long diaryId, Long authorId, String content, LocalDateTime createdAt) {
        validateContent(content);
        this.id = id;
        this.diaryId = diaryId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static DiaryComment create(Long diaryId, Long authorId, String content) {
        return new DiaryComment(null, diaryId, authorId, content, LocalDateTime.now());
    }

    public static DiaryComment reconstitute(Long id, Long diaryId, Long authorId, String content, LocalDateTime createdAt) {
        return new DiaryComment(id, diaryId, authorId, content, createdAt);
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidDiaryContentException();
        }
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
