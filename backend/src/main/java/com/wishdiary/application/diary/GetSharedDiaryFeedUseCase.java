package com.wishdiary.application.diary;

import java.util.List;

public interface GetSharedDiaryFeedUseCase {
    List<SharedDiaryResponse> getSharedFeed(Long viewerId, int page);
}
