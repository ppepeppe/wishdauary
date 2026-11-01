package com.wishdiary.application.clover;

import com.wishdiary.domain.clover.CloverLedger;
import com.wishdiary.domain.clover.CloverReason;

public interface RecordCloverLedgerPort {

    CloverLedger record(CloverLedger cloverLedger);

    boolean existsByUserIdAndReasonAndRefId(Long userId, CloverReason reason, Long refId);
}
