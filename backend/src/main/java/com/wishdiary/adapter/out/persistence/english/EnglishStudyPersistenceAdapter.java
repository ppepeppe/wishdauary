package com.wishdiary.adapter.out.persistence.english;

import com.wishdiary.application.english.EnglishStudyPort;
import com.wishdiary.domain.english.EnglishStudy;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class EnglishStudyPersistenceAdapter implements EnglishStudyPort {
    private final EnglishStudyJpaRepository repository;
    public EnglishStudyPersistenceAdapter(EnglishStudyJpaRepository repository) { this.repository = repository; }
    public Optional<EnglishStudy> findByDay(Long childId, LocalDate date) {
        return repository.findByChildUserIdAndStudyDate(childId, date).map(EnglishStudyJpaEntity::toDomain);
    }
    public Optional<Long> findChildId(Long requestId) { return repository.findChildId(requestId); }
    public Optional<EnglishStudy> findById(Long requestId) { return repository.findById(requestId).map(EnglishStudyJpaEntity::toDomain); }
    public List<EnglishStudy> findPending(Long receiverId) {
        return repository.findByReceiverFamilyUserIdAndStatusOrderByCreatedAtAsc(receiverId, EnglishStudy.Status.PENDING)
                .stream().map(EnglishStudyJpaEntity::toDomain).toList();
    }
    public EnglishStudy save(EnglishStudy study) { return repository.save(new EnglishStudyJpaEntity(study)).toDomain(); }
}
