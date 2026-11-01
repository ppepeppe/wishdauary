package com.wishdiary.application.clover;

import java.time.LocalDateTime;
import java.util.List;

public record CloverHistoryResponse(List<Entry> entries) {

    public record Entry(
            Long ledgerId,
            int amount,
            String reason,
            Long refId,
            LocalDateTime createdAt
    ) {
    }
}
