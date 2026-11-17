package com.wishdiary.application.english;

public interface ManageEnglishContentsUseCase {
    EnglishContentOverview list();
    EnglishContentResponse create(CreateEnglishContentRequest request);
    EnglishContentResponse update(Long id, UpdateEnglishContentRequest request);
}
