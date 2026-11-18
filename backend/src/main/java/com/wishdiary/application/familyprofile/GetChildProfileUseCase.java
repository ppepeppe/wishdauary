package com.wishdiary.application.familyprofile;

public interface GetChildProfileUseCase {
    ChildProfileResponse get(Long familyId, Long childId);
}
