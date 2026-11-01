package com.wishdiary.domain.diary;

import java.util.List;
import com.wishdiary.domain.sharing.InvalidSharingGroupException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Diary {

    private final List<Long> sharingGroupIds;
    private final Long id;
    private final Long authorId;
    private final String content;
    private final DiaryEmotion emotion;
    private final DiaryVisibilityScope visibilityScope;
    private final LocalDate writtenDate;
    private final LocalDateTime createdAt;

    private Diary(
            Long id,
            Long authorId,
            String content,
            DiaryEmotion emotion,
            DiaryVisibilityScope visibilityScope,
            LocalDate writtenDate,
            LocalDateTime createdAt,
            List<Long> sharingGroupIds
    ) {
        validateContent(content);
        this.sharingGroupIds = validateAudience(visibilityScope, sharingGroupIds);
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.emotion = emotion;
        this.visibilityScope = visibilityScope;
        this.writtenDate = writtenDate;
        this.createdAt = createdAt;
    }

    public static Diary create(
            Long authorId,
            String content,
            DiaryEmotion emotion,
            DiaryVisibilityScope visibilityScope,
            LocalDate writtenDate
    ) {
        return new Diary(
                null,
                authorId,
                content,
                emotion,
                visibilityScope,
                writtenDate,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static Diary reconstitute(
            Long id,
            Long authorId,
            String content,
            DiaryEmotion emotion,
            DiaryVisibilityScope visibilityScope,
            LocalDate writtenDate,
            LocalDateTime createdAt
    ) {
        return new Diary(id, authorId, content, emotion, visibilityScope, writtenDate, createdAt, List.of());
    }

    public Diary update(String content, DiaryEmotion emotion, DiaryVisibilityScope visibilityScope) {
        return new Diary(id, authorId, content, emotion, visibilityScope, writtenDate, createdAt, List.of());
    }

    public static Diary create(Long authorId, String content, DiaryEmotion emotion,
                               DiaryVisibilityScope scope, LocalDate writtenDate, List<Long> groupIds) {
        return new Diary(null, authorId, content, emotion, scope, writtenDate, LocalDateTime.now(), groupIds);
    }

    public static Diary reconstitute(Long id, Long authorId, String content, DiaryEmotion emotion,
            DiaryVisibilityScope scope, LocalDate date, LocalDateTime createdAt, List<Long> groupIds) {
        return new Diary(id, authorId, content, emotion, scope, date, createdAt, groupIds);
    }

    public Diary update(String content, DiaryEmotion emotion, DiaryVisibilityScope scope, List<Long> groupIds) {
        return new Diary(id, authorId, content, emotion, scope, writtenDate, createdAt, groupIds);
    }

    public List<Long> getSharingGroupIds() { return sharingGroupIds; }

    private static List<Long> validateAudience(DiaryVisibilityScope scope, List<Long> groupIds) {
        List<Long> ids = groupIds == null ? List.of() : groupIds;
        if (ids.stream().anyMatch(id -> id == null || id <= 0) || ids.size() > 5
                || (scope == DiaryVisibilityScope.GROUP && ids.isEmpty())
                || (scope != DiaryVisibilityScope.GROUP && !ids.isEmpty())) {
            throw new InvalidSharingGroupException("그룹 공개는 1~5개의 그룹을 선택해 주세요. 다른 공개 범위에는 그룹을 지정할 수 없어요.");
        }
        return ids.stream().distinct().toList();
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidDiaryContentException();
        }
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

    public DiaryEmotion getEmotion() {
        return emotion;
    }

    public DiaryVisibilityScope getVisibilityScope() {
        return visibilityScope;
    }

    public LocalDate getWrittenDate() {
        return writtenDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
