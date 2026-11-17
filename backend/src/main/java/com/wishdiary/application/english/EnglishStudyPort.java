package com.wishdiary.application.english;

import com.wishdiary.domain.english.EnglishStudy;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnglishStudyPort {
    Optional<EnglishStudy> findByDay(Long childId, LocalDate date);
    Optional<Long> findChildId(Long requestId);
    Optional<EnglishStudy> findById(Long requestId);
    List<EnglishStudy> findPending(Long receiverId);
    EnglishStudy save(EnglishStudy request);
}
