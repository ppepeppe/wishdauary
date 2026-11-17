package com.wishdiary.application.clover;

import com.wishdiary.application.reward.FamilyRewardAccess;
import com.wishdiary.application.reward.RewardConflictException;
import com.wishdiary.application.user.UpdateUserStreakPort;
import com.wishdiary.domain.clover.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AwardCloverService implements AwardCloverUseCase {
    private final FamilyRewardAccess access;
    private final UpdateUserStreakPort locks;
    private final FindCloverAwardPort awards;
    private final RecordCloverLedgerPort ledger;
    private final LoadCloverBalancePort balances;
    public AwardCloverService(FamilyRewardAccess access, UpdateUserStreakPort locks, FindCloverAwardPort awards,
            RecordCloverLedgerPort ledger, LoadCloverBalancePort balances) {
        this.access = access; this.locks = locks; this.awards = awards; this.ledger = ledger; this.balances = balances;
    }
    @Override
    @Transactional
    public AwardCloverResponse award(Long familyId, AwardCloverRequest request) {
        var family = access.requireFamily(familyId);
        access.requireChild(request.childUserId());
        locks.lockUser(familyId);
        locks.lockUser(request.childUserId());
        access.requireConnection(request.childUserId(), familyId);
        String key = request.requestKey().toString();
        var previous = awards.findByRequestKey(familyId, key);
        CloverLedger saved = previous.orElseGet(() -> ledger.record(CloverLedger.create(request.childUserId(),
                request.amount().intValueExact(), CloverReason.FAMILY_AWARD, null)
                .withDetails(new CloverDetails(request.reason().trim(), familyId, access.displayName(family), key))));
        if (!saved.getUserId().equals(request.childUserId()) || saved.getAmount() != request.amount().intValueExact()
                || !saved.getDetails().note().equals(request.reason().trim())) {
            throw new RewardConflictException("이미 처리한 지급 요청이에요. 새 지급을 시작해 주세요.");
        }
        return new AwardCloverResponse(saved.getId(), saved.getUserId(), saved.getAmount(), saved.getDetails().note(),
                balances.loadBalance(request.childUserId()));
    }
}
