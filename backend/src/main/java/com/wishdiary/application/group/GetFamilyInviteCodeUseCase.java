package com.wishdiary.application.group;

public interface GetFamilyInviteCodeUseCase {

    FamilyInviteCodeResponse getInviteCode(Long requesterUserId);
}
