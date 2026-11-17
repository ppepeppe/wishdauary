package com.wishdiary.adapter.out.persistence.english;

import com.wishdiary.domain.english.EnglishStudy;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "english_studies", uniqueConstraints = @UniqueConstraint(name = "uk_english_child_day", columnNames = {"childUserId", "studyDate"}))
public class EnglishStudyJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long childUserId;
    @Column(nullable = false) private Long receiverFamilyUserId;
    @Column(nullable = false) private LocalDate studyDate;
    @Column(nullable = false, length = 120) private String sentence;
    @Column(nullable = false, length = 120) private String meaning;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EnglishStudy.Status status;
    @Column(nullable = false) private Instant createdAt;
    @Column private Instant processedAt;
    protected EnglishStudyJpaEntity() {}
    EnglishStudyJpaEntity(EnglishStudy study) {
        id = study.id(); childUserId = study.childUserId(); receiverFamilyUserId = study.receiverFamilyUserId();
        studyDate = study.studyDate(); sentence = study.sentence(); meaning = study.meaning(); status = study.status();
        createdAt = study.createdAt(); processedAt = study.processedAt();
    }
    EnglishStudy toDomain() {
        return new EnglishStudy(id, childUserId, receiverFamilyUserId, studyDate, sentence, meaning, status, createdAt, processedAt);
    }
}
