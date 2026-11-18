package com.wishdiary.application.familyprofile;

import com.wishdiary.application.clover.*;
import com.wishdiary.application.reward.FamilyRewardAccess;
import com.wishdiary.application.shop.*;
import com.wishdiary.application.user.LoadUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChildProfileService implements GetChildProfileUseCase {
    private final FamilyRewardAccess access;
    private final LoadUserPort users;
    private final GetCloverBalanceUseCase balances;
    private final GetCloverHistoryUseCase history;
    private final GetSentGiftRequestsUseCase gifts;

    public ChildProfileService(FamilyRewardAccess access, LoadUserPort users, GetCloverBalanceUseCase balances,
            GetCloverHistoryUseCase history, GetSentGiftRequestsUseCase gifts) {
        this.access = access; this.users = users; this.balances = balances; this.history = history; this.gifts = gifts;
    }

    @Transactional(readOnly = true)
    public ChildProfileResponse get(Long familyId, Long childId) {
        access.requireFamily(familyId);
        access.requireChild(childId);
        access.requireConnection(childId, familyId);
        return new ChildProfileResponse(childId, users.loadById(childId).nickname(), balances.get(childId),
                history.getHistory(childId), gifts.getSentRequests(childId));
    }

}
