package com.wishdiary.adapter.out.persistence.diary;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryJpaRepository extends JpaRepository<DiaryJpaEntity, Long> {

    @Query(value = """
            select d.* from diaries d
            where d.visibility_scope = 'PUBLIC'
               or (d.visibility_scope = 'GROUP' and (d.author_id = :viewerId or exists (
                   select 1 from diary_audience_groups a
                   join diary_sharing_groups g on g.id = a.sharing_group_id
                   join diary_sharing_group_members m on m.sharing_group_id = g.id
                   where a.diary_id = d.id and g.owner_id = d.author_id
                     and g.slot is not null and m.user_id = :viewerId
               )))
            order by d.created_at desc, d.id desc
            """, nativeQuery = true)
    List<DiaryJpaEntity> findSharedFeed(@Param("viewerId") Long viewerId,
                                      Pageable pageable);


    boolean existsByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate);

    long countByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate);

    Optional<DiaryJpaEntity> findTopByAuthorIdAndWrittenDateOrderByCreatedAtDescIdDesc(Long authorId, LocalDate writtenDate);

    List<DiaryJpaEntity> findAllByAuthorIdOrderByWrittenDateDescCreatedAtDesc(Long authorId);

    List<DiaryJpaEntity> findAllByAuthorIdInAndVisibilityScopeInOrderByWrittenDateDescCreatedAtDesc(
            List<Long> authorIds,
            List<String> visibilityScopes
    );
}
