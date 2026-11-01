package com.wishdiary.application.diary;

public interface UpdateDiaryUseCase {

    DiaryDetailResponse update(Long requesterUserId, Long diaryId, UpdateDiaryRequest request);
}
