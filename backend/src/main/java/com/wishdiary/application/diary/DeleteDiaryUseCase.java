package com.wishdiary.application.diary;

public interface DeleteDiaryUseCase {

    void delete(Long requesterUserId, Long diaryId);
}
