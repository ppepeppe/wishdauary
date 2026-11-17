package com.wishdiary.application.clover;

import com.wishdiary.domain.clover.CloverLedger;
import java.util.Optional;

public interface FindCloverAwardPort {
    Optional<CloverLedger> findByRequestKey(Long familyUserId, String requestKey);
}
