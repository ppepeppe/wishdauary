package com.wishdiary.domain.clover;

import java.time.LocalDateTime;

public class CloverLedger {

    private final Long id;
    private final Long userId;
    private final int amount;
    private final CloverReason reason;
    private final Long refId;
    private final LocalDateTime createdAt;
    private final CloverDetails details;

    private CloverLedger(
            Long id,
            Long userId,
            int amount,
            CloverReason reason,
            Long refId,
            LocalDateTime createdAt
    ) {
        this(id, userId, amount, reason, refId, createdAt, null);
    }

    private CloverLedger(Long id, Long userId, int amount, CloverReason reason, Long refId,
            LocalDateTime createdAt, CloverDetails details) {
        this.details = details;
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.refId = refId;
        this.createdAt = createdAt;
    }

    public static CloverLedger create(Long userId, int amount, CloverReason reason, Long refId) {
        return new CloverLedger(
                null,
                userId,
                amount,
                reason,
                refId,
                LocalDateTime.now()
        );
    }

    public static CloverLedger reconstitute(
            Long id,
            Long userId,
            int amount,
            CloverReason reason,
            Long refId,
            LocalDateTime createdAt
    ) {
        return new CloverLedger(id, userId, amount, reason, refId, createdAt);
    }

    public CloverLedger withDetails(CloverDetails details) {
        return new CloverLedger(id, userId, amount, reason, refId, createdAt, details);
    }

    public CloverDetails getDetails() { return details; }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public int getAmount() {
        return amount;
    }

    public CloverReason getReason() {
        return reason;
    }

    public Long getRefId() {
        return refId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
