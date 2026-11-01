package com.wishdiary.application.diary;

import java.util.List;

public interface GetFamilyDiaryFeedUseCase {

    List<FamilyDiaryFeedResponse> getFamilyFeed(Long familyUserId);
}
