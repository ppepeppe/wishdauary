package com.wishdiary.application.diary;

import java.util.List;

public interface GetMyDiaryListUseCase {

    List<MyDiarySummaryResponse> get(Long authorId);
}
