package com.wishdiary.application.diary;

import com.wishdiary.application.sharing.SharingGroupPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.diary.DiaryVisibilityScope;
import com.wishdiary.domain.sharing.InvalidSharingGroupException;
import org.springframework.stereotype.Component;

@Component
public class DiaryAudiencePolicy {
    private final SharingGroupPort groups;

    private final FamilyConnectionPort familyConnections;

    public DiaryAudiencePolicy(SharingGroupPort groups, FamilyConnectionPort familyConnections) {
        this.groups = groups;
        this.familyConnections = familyConnections;
    }

    public void validateSelection(Diary diary) {
        if (diary.getVisibilityScope() != DiaryVisibilityScope.GROUP) return;
        groups.lockOwner(diary.getAuthorId());
        for (Long groupId : diary.getSharingGroupIds()) {
            groups.findById(groupId)
                    .filter(group -> group.active() && group.ownerId().equals(diary.getAuthorId()))
                    .orElseThrow(() -> new InvalidSharingGroupException("내가 만든 공개 그룹만 선택할 수 있어요. 삭제된 그룹은 다시 선택해 주세요."));
        }
    }

    public boolean canRead(LoadedUser viewer, Diary diary, LoadedUser author) {
        if (viewer.id().equals(diary.getAuthorId())) return true;
        return switch (diary.getVisibilityScope()) {
            case PUBLIC -> true;
            case FAMILY -> viewer.isFamily() && familyConnections.isConnected(diary.getAuthorId(), viewer.id());
            case GROUP -> groups.isMemberOfAny(diary.getAuthorId(), diary.getSharingGroupIds(), viewer.id());
            default -> false;
        };
    }
}
