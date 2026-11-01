package com.wishdiary.application.group;

public interface RemoveFamilyMemberUseCase {

    void removeMember(Long requesterUserId, Long groupMemberId);
}
