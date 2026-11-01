package com.wishdiary.application.reaction;

import com.wishdiary.domain.diary.Diary;
import java.util.Optional;

public interface ReactionTargetDiaryPort {

    Optional<Diary> loadById(Long diaryId);
}
