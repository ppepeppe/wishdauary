package com.wishdiary.application.group;

import java.util.List;

public interface GetFamilyMembersUseCase {

    List<FamilyMemberResponse> getMembers(Long requesterUserId);
}
