package com.wishdiary.adapter.out.persistence.english;

import com.wishdiary.domain.english.EnglishStudy;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EnglishStudyJpaRepository extends JpaRepository<EnglishStudyJpaEntity, Long> {
    Optional<EnglishStudyJpaEntity> findByChildUserIdAndStudyDate(Long childId, LocalDate date);
    List<EnglishStudyJpaEntity> findByReceiverFamilyUserIdAndStatusOrderByCreatedAtAsc(Long receiverId, EnglishStudy.Status status);
    @Query("select e.childUserId from EnglishStudyJpaEntity e where e.id = :id")
    Optional<Long> findChildId(@Param("id") Long requestId);
}
