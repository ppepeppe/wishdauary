package com.wishdiary.application.user;

import java.util.List;
import java.util.Map;

public interface LoadUserPort {

    LoadedUser loadById(Long userId);

    List<LoadedUser> loadChildrenByGroupId(Long groupId);

    List<LoadedUser> loadFamilyMembersByGroupId(Long groupId);

    Map<Long, LoadedUser> loadByIds(List<Long> userIds);
}
