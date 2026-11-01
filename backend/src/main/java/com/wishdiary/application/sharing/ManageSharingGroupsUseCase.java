package com.wishdiary.application.sharing;

import java.util.List;

public interface ManageSharingGroupsUseCase {
    List<SharingUser> searchUsers(Long childId, String query);
    List<SharingGroupResponse> list(Long childId);
    SharingGroupResponse create(Long childId, SharingGroupRequest request);
    SharingGroupResponse update(Long childId, Long groupId, SharingGroupRequest request);
    void delete(Long childId, Long groupId);
}
