package com.wishdiary.application.sharing;

import java.util.List;
import java.util.Set;

public record SharingGroupResponse(Long id, String name, Set<Long> memberIds, List<SharingUser> members, boolean defaultGroup) { }
