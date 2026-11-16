package com.wishdiary.application.user;

import com.wishdiary.domain.user.ProfileAvatar;
import com.wishdiary.domain.user.FamilyRelationship;
import com.wishdiary.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyProfileService implements GetMyProfileUseCase, UpdateMyProfileUseCase {

    private final LoadUserAccountPort loadUserAccountPort;
    private final SaveUserProfilePort saveUserProfilePort;
    private final LoadFamilyRelationshipPort loadFamilyRelationshipPort;
    private final SaveFamilyRelationshipPort saveFamilyRelationshipPort;

    public MyProfileService(
            LoadUserAccountPort loadUserAccountPort,
            SaveUserProfilePort saveUserProfilePort,
            LoadFamilyRelationshipPort loadFamilyRelationshipPort,
            SaveFamilyRelationshipPort saveFamilyRelationshipPort
    ) {
        this.loadUserAccountPort = loadUserAccountPort;
        this.saveUserProfilePort = saveUserProfilePort;
        this.loadFamilyRelationshipPort = loadFamilyRelationshipPort;
        this.saveFamilyRelationshipPort = saveFamilyRelationshipPort;
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
        FamilyRelationship relationship = request.relationLabel() == null ? null
                : FamilyRelationship.forUser(user.appRole(), request.relationLabel());
        User updated = user.changeProfile(
                request.nickname(),
                ProfileAvatar.of(request.avatarPreset(), request.avatarImage(),
                        request.avatarCharacter() == null ? user.avatar().character() : request.avatarCharacter())
        );
        User saved = saveUserProfilePort.saveProfile(updated);
        if (relationship != null) saveFamilyRelationshipPort.saveRelationship(userId, relationship);
        return toResponse(saved);
    }

    private MyProfileResponse toResponse(User user) {
        return new MyProfileResponse(
                user.id(),
                user.loginId(),
                user.nickname(),
                user.appRole().name().toLowerCase(),
                user.avatar().preset(),
                user.avatar().imageDataUrl(),
                user.avatar().character(),
                user.userRole().name().toLowerCase(),
                user.appRole().isFamily() ? loadFamilyRelationshipPort.loadRelationLabel(user.id()) : null
        );
    }
}
