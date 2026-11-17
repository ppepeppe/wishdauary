package com.wishdiary.adapter.out.persistence.english;

import com.wishdiary.application.english.EnglishContentPort;
import com.wishdiary.application.english.EnglishContentUnavailableException;
import com.wishdiary.domain.english.EnglishContent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EnglishContentPersistenceAdapter implements EnglishContentPort {
    private final EnglishContentJpaRepository contents;
    private final EnglishDailyContentJpaRepository daily;
    public EnglishContentPersistenceAdapter(EnglishContentJpaRepository contents, EnglishDailyContentJpaRepository daily) {
        this.contents = contents; this.daily = daily;
    }
    public List<EnglishContent> list() { return contents.findAllByOrderByIdAsc().stream().map(EnglishContentJpaEntity::toDomain).toList(); }
    public Optional<EnglishContent> findById(Long id) { return contents.findById(id).map(EnglishContentJpaEntity::toDomain); }
    public Optional<EnglishContent> findForUpdate(Long id) { return contents.findForUpdate(id).map(EnglishContentJpaEntity::toDomain); }
    public EnglishContent save(EnglishContent content) { return contents.saveAndFlush(new EnglishContentJpaEntity(content)).toDomain(); }
    public void lockSelection() { contents.findFirstByOrderByIdAsc().orElseThrow(EnglishContentUnavailableException::new); }
    public Optional<Long> findScheduledId(LocalDate date) { return daily.findContentId(date); }
    public void schedule(LocalDate date, Long contentId) { daily.saveAndFlush(new EnglishDailyContentJpaEntity(date, contentId)); }
}
