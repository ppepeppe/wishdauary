package com.wishdiary.application.user;

public interface UpdateMyProfileUseCase {

    MyProfileResponse update(Long userId, UpdateMyProfileRequest request);
}
