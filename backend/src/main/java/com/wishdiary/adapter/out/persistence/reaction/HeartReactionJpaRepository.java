package com.wishdiary.adapter.out.persistence.reaction;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HeartReactionJpaRepository extends JpaRepository<HeartReactionJpaEntity, Long> {

    Optional<HeartReactionJpaEntity> findByDiaryIdAndFamilyUserId(Long diaryId, Long familyUserId);

    List<HeartReactionJpaEntity> findAllByDiaryIdIn(List<Long> diaryIds);

    void deleteAllByDiaryId(Long diaryId);

    long countByDiaryId(Long diaryId);

    @Query("""
            select reaction.diaryId as diaryId, count(reaction.id) as heartCount
            from HeartReactionJpaEntity reaction
            where reaction.diaryId in :diaryIds
            group by reaction.diaryId
            """)
    List<HeartReactionCountProjection> countGroupedByDiaryIds(List<Long> diaryIds);

    interface HeartReactionCountProjection {
        Long getDiaryId();

        Long getHeartCount();
    }
}
