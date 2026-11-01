package com.wishdiary.domain.sharing;

import static org.junit.jupiter.api.Assertions.*;
import com.wishdiary.domain.diary.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SharingGroupTest {
    @Test
    void groupNamesAndSlotsAreValidated() {
        assertThrows(InvalidSharingGroupException.class, () -> new SharingGroup(null, 1L, " ", 1, Set.of()));
        assertThrows(InvalidSharingGroupException.class, () -> new SharingGroup(null, 1L, "a".repeat(31), 1, Set.of()));
        assertThrows(InvalidSharingGroupException.class, () -> new SharingGroup(null, 1L, "friends", 6, Set.of()));
        assertEquals("friends", new SharingGroup(null, 1L, " friends ", 1, Set.of(2L)).name());
    }

    @Test
    void deletingGroupRetainsIdentityButRevokesMembersAndReleasesSlot() {
        SharingGroup group = new SharingGroup(10L, 1L, "friends", 1, Set.of(2L));
        SharingGroup deleted = group.delete();
        assertEquals(10L, deleted.id());
        assertFalse(deleted.active());
        assertTrue(deleted.memberIds().isEmpty());
        assertTrue(group.active());
    }

    @Test
    void groupDiaryRequiresOneToFiveGroupsAndOtherScopesRejectGroupIds() {
        assertThrows(InvalidSharingGroupException.class, () -> diary(DiaryVisibilityScope.GROUP, List.of()));
        assertThrows(InvalidSharingGroupException.class, () -> diary(DiaryVisibilityScope.GROUP, List.of(1L,2L,3L,4L,5L,6L)));
        assertThrows(InvalidSharingGroupException.class, () -> diary(DiaryVisibilityScope.GROUP, List.of(-1L)));
        assertThrows(InvalidSharingGroupException.class, () -> diary(DiaryVisibilityScope.PUBLIC, List.of(1L)));
        assertEquals(List.of(1L, 2L), diary(DiaryVisibilityScope.GROUP, List.of(1L,2L,1L)).getSharingGroupIds());
        assertEquals(List.of(), diary(DiaryVisibilityScope.PRIVATE, null).getSharingGroupIds());
    }

    private Diary diary(DiaryVisibilityScope scope, List<Long> ids) {
        return Diary.create(1L, "hello", null, scope, LocalDate.now(), ids);
    }
}
