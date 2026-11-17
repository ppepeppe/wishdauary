package com.wishdiary.application.reward;

import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.FamilyConnectionAccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class FamilyRewardAccess {
    private final LoadUserPort users;
    private final FamilyConnectionPort connections;
    public FamilyRewardAccess(LoadUserPort users, FamilyConnectionPort connections) {
        this.users = users; this.connections = connections;
    }
    public LoadedUser requireFamily(Long id) {
        LoadedUser user = users.loadById(id);
        if (!user.isFamily()) throw new FamilyConnectionAccessDeniedException();
        return user;
    }
    public void requireChild(Long id) {
        if (!users.loadById(id).isChild()) throw new FamilyConnectionAccessDeniedException();
    }
    public void requireConnection(Long childId, Long familyId) {
        if (!connections.isConnected(childId, familyId)) throw new FamilyConnectionAccessDeniedException();
    }
    public String displayName(LoadedUser user) {
        return user.relationLabel() == null || user.relationLabel().isBlank() || user.relationLabel().equals(user.nickname())
                ? user.nickname() : user.nickname() + " (" + user.relationLabel() + ")";
    }
}
