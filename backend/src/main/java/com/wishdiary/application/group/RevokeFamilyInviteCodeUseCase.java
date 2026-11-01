package com.wishdiary.application.group;

public interface RevokeFamilyInviteCodeUseCase {

    FamilyInviteCodeResponse revokeInviteCode(Long requesterUserId);
}
