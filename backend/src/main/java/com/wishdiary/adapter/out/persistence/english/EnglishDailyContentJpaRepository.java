package com.wishdiary.adapter.out.persistence.english;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EnglishDailyContentJpaRepository extends JpaRepository<EnglishDailyContentJpaEntity, LocalDate> {
    @Query("select d.contentId from EnglishDailyContentJpaEntity d where d.studyDate = :date")
    Optional<Long> findContentId(@Param("date") LocalDate date);
}
