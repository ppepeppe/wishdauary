package com.wishdiary.application.diary;

import com.wishdiary.domain.diary.Diary;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoadDiaryQueryPort {

    boolean existsByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate);

    long countByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate);

    Optional<Diary> loadLatestByAuthorIdAndWrittenDate(Long authorId, LocalDate writtenDate);

    List<Diary> loadAllByAuthorId(Long authorId);

    List<Diary> loadFamilyVisibleByAuthorIds(List<Long> authorIds);

    Optional<Diary> loadById(Long diaryId);
}
