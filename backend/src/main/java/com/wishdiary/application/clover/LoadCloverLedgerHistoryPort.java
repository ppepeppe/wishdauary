package com.wishdiary.application.clover;

import com.wishdiary.domain.clover.CloverLedger;
import java.util.List;

public interface LoadCloverLedgerHistoryPort {

    List<CloverLedger> loadAllByUserId(Long userId);
}
