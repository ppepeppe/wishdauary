package com.wishdiary.application.user;

import com.wishdiary.domain.user.ProfileAvatar;
import com.wishdiary.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyProfileService implements GetMyProfileUseCase, UpdateMyProfileUseCase {

    private final LoadUserAccountPort loadUserAccountPort;
    private final SaveUserProfilePort saveUserProfilePort;

    public MyProfileService(
            LoadUserAccountPort loadUserAccountPort,
            SaveUserProfilePort saveUserProfilePort
    ) {
        this.loadUserAccountPort = loadUserAccountPort;
        this.saveUserProfilePort = saveUserProfilePort;
    }

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse get(Long userId) {
        return toResponse(loadUserAccountPort.loadAccount(userId));
    }

    @Override
    @Transactional
    public MyProfileResponse update(Long userId, UpdateMyProfileRequest request) {
        User user = loadUserAccountPort.loadAccount(userId);
        User updated = user.changeProfile(
                request.nickname(),
                ProfileAvatar.of(request.avatarPreset(), request.avatarImage())
        );
        return toResponse(saveUserProfilePort.saveProfile(updated));
    }

    private MyProfileResponse toResponse(User user) {
        return new MyProfileResponse(
                user.id(),
                user.email(),
                user.nickname(),
                user.appRole().name().toLowerCase(),
                user.avatar().preset(),
                user.avatar().imageDataUrl()
        );
    }
}
