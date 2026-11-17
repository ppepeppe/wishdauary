package com.wishdiary.adapter.out.persistence.english;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "english_daily_contents")
public class EnglishDailyContentJpaEntity {
    @Id private LocalDate studyDate;
    @Column(nullable = false) private Long contentId;
    protected EnglishDailyContentJpaEntity() {}
    EnglishDailyContentJpaEntity(LocalDate date, Long contentId) { this.studyDate = date; this.contentId = contentId; }
}
