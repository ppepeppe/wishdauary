package com.wishdiary.application.diary;

import com.wishdiary.domain.diary.DiaryPhoto;

public interface DecodeDiaryPhotoPort {
    DiaryPhoto decode(String imageData);
}
