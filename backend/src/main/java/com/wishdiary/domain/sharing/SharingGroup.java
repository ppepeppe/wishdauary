package com.wishdiary.domain.sharing;

import java.util.Set;

public record SharingGroup(Long id, Long ownerId, String name, Integer slot, Set<Long> memberIds) {
    public static final int MAX_GROUPS = 5;
    public static final int FAMILY_GROUP_SLOT = 0;

    public SharingGroup {
        if (name == null || name.isBlank() || name.trim().length() > 30) {
            throw new InvalidSharingGroupException("그룹 이름은 1~30자로 입력해 주세요.");
        }
        if (slot != null && (slot < FAMILY_GROUP_SLOT || slot > MAX_GROUPS)) {
            throw new InvalidSharingGroupException("공개 그룹은 최대 5개까지 만들 수 있어요.");
        }
        if (memberIds == null || memberIds.stream().anyMatch(idValue -> idValue == null || idValue <= 0)) {
            throw new InvalidSharingGroupException("그룹 구성원을 다시 확인해 주세요.");
        }
        name = name.trim();
        memberIds = Set.copyOf(memberIds);
    }

    public boolean active() { return slot != null; }

    public SharingGroup update(String newName, Set<Long> newMembers) {
        return new SharingGroup(id, ownerId, newName, slot, newMembers);
    }

    public SharingGroup delete() {
        return new SharingGroup(id, ownerId, name, null, Set.of());
    }
}
