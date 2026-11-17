package com.wishdiary.application.english;

import com.wishdiary.domain.english.DailyEnglish;
import com.wishdiary.domain.english.EnglishContent;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyEnglishContentService implements GetDailyEnglishContentUseCase {
    private final EnglishContentPort contents;
    public DailyEnglishContentService(EnglishContentPort contents) { this.contents = contents; }
    @Override
    @Transactional
    public EnglishContent get(LocalDate date) {
        Long id = contents.findScheduledId(date).orElseGet(() -> select(date));
        return contents.findById(id).orElseThrow(EnglishContentUnavailableException::new);
    }
    private Long select(LocalDate date) {
        // Serialize first selection of the day, then recheck after acquiring the lock.
        contents.lockSelection();
        var selected = contents.findScheduledId(date);
        if (selected.isPresent()) return selected.get();
        var pool = contents.list();
        if (pool.isEmpty()) throw new EnglishContentUnavailableException();
        Long id = pool.get(DailyEnglish.indexFor(date, pool.size())).id();
        contents.schedule(date, id);
        return id;
    }
}
