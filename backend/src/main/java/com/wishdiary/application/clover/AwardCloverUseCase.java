package com.wishdiary.application.clover;

public interface AwardCloverUseCase {
    AwardCloverResponse award(Long familyUserId, AwardCloverRequest request);
}
