package com.wishdiary.application.diary;

import com.wishdiary.domain.diary.DiaryPhoto;
import java.util.Optional;

public interface DiaryPhotoPort {
    void save(Long diaryId, DiaryPhoto photo);
    Optional<DiaryPhoto> load(Long diaryId);
    void delete(Long diaryId);
}
