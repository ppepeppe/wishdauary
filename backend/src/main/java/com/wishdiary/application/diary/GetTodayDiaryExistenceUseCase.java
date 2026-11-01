package com.wishdiary.application.diary;

import java.time.LocalDate;

public interface GetTodayDiaryExistenceUseCase {

    TodayDiaryExistenceResponse get(Long authorId, LocalDate date);
}
