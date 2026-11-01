package com.wishdiary.application.reaction;

import java.util.List;

public interface GetDiaryCommentsUseCase {

    List<DiaryCommentResponse> getComments(Long diaryId, Long viewerUserId);
}
