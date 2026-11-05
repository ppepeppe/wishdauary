package com.wishdiary.adapter.out.persistence.reaction;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiaryCommentJpaRepository extends JpaRepository<DiaryCommentJpaEntity, Long> {

    List<DiaryCommentJpaEntity> findAllByDiaryIdOrderByCreatedAtAscIdAsc(Long diaryId);

    List<DiaryCommentJpaEntity> findAllByDiaryIdIn(List<Long> diaryIds);

    void deleteAllByDiaryId(Long diaryId);

    @Query("""
            select comment.diaryId as diaryId, count(comment.id) as commentCount
            from DiaryCommentJpaEntity comment
            where comment.diaryId in :diaryIds
            group by comment.diaryId
            """)
    List<DiaryCommentCountProjection> countGroupedByDiaryIds(List<Long> diaryIds);

    interface DiaryCommentCountProjection {
        Long getDiaryId();

        Long getCommentCount();
    }
}
