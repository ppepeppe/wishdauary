package com.wishdiary.application.english;

import com.wishdiary.domain.english.EnglishContent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnglishContentPort {
    List<EnglishContent> list();
    Optional<EnglishContent> findById(Long id);
    Optional<EnglishContent> findForUpdate(Long id);
    EnglishContent save(EnglishContent content);
    void lockSelection();
    Optional<Long> findScheduledId(LocalDate date);
    void schedule(LocalDate date, Long contentId);
}
