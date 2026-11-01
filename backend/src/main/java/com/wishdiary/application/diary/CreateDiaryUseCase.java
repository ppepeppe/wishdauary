package com.wishdiary.application.diary;

public interface CreateDiaryUseCase {

    CreateDiaryResponse create(Long authorId, CreateDiaryRequest request);
}
