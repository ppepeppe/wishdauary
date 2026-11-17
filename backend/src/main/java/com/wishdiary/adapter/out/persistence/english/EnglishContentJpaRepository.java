package com.wishdiary.adapter.out.persistence.english;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EnglishContentJpaRepository extends JpaRepository<EnglishContentJpaEntity, Long> {
    List<EnglishContentJpaEntity> findAllByOrderByIdAsc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EnglishContentJpaEntity> findFirstByOrderByIdAsc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from EnglishContentJpaEntity c where c.id = :id")
    Optional<EnglishContentJpaEntity> findForUpdate(@Param("id") Long id);
}
