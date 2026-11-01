package com.wishdiary.application.diary;

import com.wishdiary.domain.diary.Diary;
import java.util.List;

public interface LoadSharedDiaryFeedPort {
    List<Diary> loadSharedFeed(Long viewerId, int page);
}
