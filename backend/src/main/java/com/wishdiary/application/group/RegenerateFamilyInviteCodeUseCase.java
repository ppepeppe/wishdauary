package com.wishdiary.application.group;

public interface RegenerateFamilyInviteCodeUseCase {

    FamilyInviteCodeResponse regenerateInviteCode(Long requesterUserId);
}
