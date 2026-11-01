package com.wishdiary.application.reaction;

public interface CreateDiaryCommentUseCase {

    DiaryCommentResponse create(Long familyUserId, CreateDiaryCommentRequest request);
}
