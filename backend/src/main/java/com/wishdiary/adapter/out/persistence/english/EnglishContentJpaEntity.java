package com.wishdiary.adapter.out.persistence.english;

import com.wishdiary.domain.english.EnglishContent;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "english_contents")
public class EnglishContentJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String sentence;
    @Column(nullable = false, length = 120) private String meaning;
    @Version @Column(nullable = false) private Long version;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    protected EnglishContentJpaEntity() {}
    EnglishContentJpaEntity(EnglishContent content) {
        id = content.id(); sentence = content.sentence(); meaning = content.meaning();
        version = id == null ? null : content.version(); createdAt = content.createdAt(); updatedAt = content.updatedAt();
    }
    EnglishContent toDomain() { return new EnglishContent(id, sentence, meaning, version, createdAt, updatedAt); }
}
