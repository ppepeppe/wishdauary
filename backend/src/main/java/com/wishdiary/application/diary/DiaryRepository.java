package com.wishdiary.application.diary;

import com.wishdiary.domain.diary.Diary;

public interface DiaryRepository {

    Diary save(Diary diary);

    void deleteById(Long diaryId);
}
