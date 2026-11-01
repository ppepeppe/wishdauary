package com.wishdiary.application.diary;

public interface GetDiaryDetailUseCase {

    DiaryDetailResponse getDetail(Long viewerUserId, Long diaryId);
}
